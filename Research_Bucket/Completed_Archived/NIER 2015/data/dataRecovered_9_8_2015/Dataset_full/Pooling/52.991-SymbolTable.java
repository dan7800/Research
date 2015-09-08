/*-*- Mode: java; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 0 -*-*/
/*.IH,SymbolTable,======================================*/
/*.IC,--- COPYRIGHT (c) --  Financial Toolsmiths - Sweden 1991-2001 ---

                      All Rights Reserved

Permission to use, copy, modify, and distribute this software and its
documentation for NON-COMMERCIAL purposes and without fee is hereby granted,
provided that the above copyright notice appear in all copies and that
both that copyright notice and this permission notice appear in
supporting documentation, and that the name of Financial Toolsmiths AB
not be used in advertising or publicity pertaining to distribution of the
software without specific, written prior permission.

FINANCIAL TOOLSMITHS AB DISCLAIMS ALL WARRANTIES WITH REGARD TO THIS 
SOFTWARE OR ITS DERIVATIVES,
INCLUDING ALL IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS,IN NO 
EVENT SHALL FINANCIAL TOOLSMITHS AB BE LIABLE FOR ANY SPECIAL, INDIRECT 
OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES WHATSOEVER RESULTING FROM LOSS 
OF USE, DATA OR PROFITS, WHETHER IN AN ACTION OF CONTRACT, NEGLIGENCE 
OR OTHER TORTIOUS ACTION, ARISING OUT OF OR IN CONNECTION WITH THE USE 
OR PERFORMANCE OF THIS SOFTWARE OR ITS DERIVATIVES.

---------------------------------------------------------------------*/
/*.IA,	PUBLIC Include File SymbolTable.java			*/

package org.openebxml.comp.bml;

/************************************************
	Includes
\************************************************/

/**
 *  Class SymbolTable
 *
 * @author Anders W. Tell   Financial Toolsmiths AB
 * @version $Id: SymbolTable.java,v 1.4 2002/05/12 10:30:26 awtopensource Exp $
 */

public final class SymbolTable {
    static final int SYMBOL_SIZE        = 256;
    static final int SYMBOL_MASK        = 0x000000FF;
    static final int SYMBOLPAGE_MASK    = 0xFFFFFF00;

    static final int SYMBOLS_InitialSize     = 65;

    int           fSymbols_Max            = 0;

    /*------------------------------*/
    /*  Unique table                */
    /*------------------------------*/
    transient SymbolEntry   fHash[]             = new SymbolEntry[SYMBOLS_InitialSize];;
    transient int			fHash_No 	        = 0;
    float 					fHash_LoadFactor     = 0.75f;
    int 					fHash_Threshold      = (int)(SYMBOLS_InitialSize * fHash_LoadFactor);
    transient int 			fHash_ModCount 	    = 0;


    /*------------------------------*/
    /*  Indexed access              */
    /*------------------------------*/
    SymbolPageReference	fPages[] = new SymbolPageReference[SYMBOLS_InitialSize];
    int		fPages_No 	        = 0;
    float 	fPages_LoadFactor   = 0.75f;
    int     fPages_Threshold = (int)(SYMBOLS_InitialSize *fHash_LoadFactor);
    int     fPages_ModCount 	= 0;

    /****/
    public  SymbolTable()
    {
        Preallocattion_Add();
    }

    final void Preallocattion_Add()
    {
		SymbolPage	lPre	= BMLSTATIC.getPreallocated();
        SymbolPage	lAdded	= Page_Add(-1, lPre);
        
        for(int i=-256;i<-0;i++)
            {
            Symbol  lSymbol = lPre.fItems[(i & SYMBOL_MASK)];
            if( lSymbol != null )
                {
                Hashtable_Add(i, lSymbol , true);
                }
            }/*for*/
    }
    
    /****/
    public final void Reset()
    {
        fHash               = new SymbolEntry[SYMBOLS_InitialSize];
        fHash_No 	        = 0;
        fHash_ModCount 	    = 0;

        fPages              = new SymbolPageReference[SYMBOLS_InitialSize];
        fPages_No 	        = 0;
        fPages_ModCount 	= 0;

        fSymbols_Max        = 0;

        Preallocattion_Add();
    }



    /**
     *
     */
    public final int getSymbolsMax()
    {
        return fSymbols_Max;
    }

    /*-----------------------------------------------------------------*/
    /*-----------------------------------------------------------------*/
    /**
     *
     */
     final int	Hashtable_No(){
		return fHash_No;
    }

    /**
     *
     */
     final void Hashtable_Rehash() 
    {
        Hashtable_Rehash((fHash.length*2) +1);
    }
    /**
     *
     */
     final void Hashtable_Rehash(int newCapacity) 
    {
		int 			liOldLength = fHash.length;
		SymbolEntry  	oldMap[] 	= fHash;
        SymbolEntry     newMap[]	= new SymbolEntry[newCapacity];
		
		fHash_ModCount++;
		fHash_Threshold = (int)(newCapacity * fHash_LoadFactor);
		fHash = newMap;
		
		for (int i = liOldLength ; i-- > 0 ;) 
			{
			for (SymbolEntry old = oldMap[i] ; old != null ; ) 
				{
				SymbolEntry lElem = old;
				old = old.getNext();
				
				int index = (lElem.hashCode() & 0x7FFFFFFF) % newCapacity;
				lElem.setNext(newMap[index]);
				newMap[index] = lElem;
				}/*for*/
			}/*for*/
    }


	/**
	 *
	 */
    public final SymbolEntry Hashtable_Add(int     idx, 
                                    Symbol  item, 
                                    boolean allowDuplicates)
    {
		if (item == null) 
			{throw new IllegalArgumentException("Null Symbol");}
		
        SymbolEntry   lTable[]    = fHash;
        int           liHash      = item.hashCode();
        int           liIndex     = (liHash & 0x7FFFFFFF) % lTable.length;

        if( !allowDuplicates )
			{
            /* Find in table */
            for (SymbolEntry lEntry = lTable[liIndex] ; 
                 lEntry != null ; 
                 lEntry = lEntry.getNext()) 
                {
                if (lEntry.hashCode() == liHash && lEntry.equals(item)) 
                    {/* already exists */
                    return lEntry;
                    }
                }/*for*/
            }/*if*/
        
		fHash_ModCount++;

		/* Check if need to rehash */
		if (fHash_No >= fHash_Threshold) 
			{
			Hashtable_Rehash();
			
			lTable	= fHash;
            liIndex = (liHash & 0x7FFFFFFF) % lTable.length;
			}/*if*/
		
		/*-- Add to table --*/
        SymbolEntry   lNew = new SymbolEntry(idx,item,lTable[liIndex]);
		lTable[liIndex] = lNew;
		fHash_No++;

		return lNew;
    }

    /**
     *
     */
     final SymbolEntry	Hashtable_Remove(Symbol item)
    {
		SymbolEntry lTable[]  = fHash;
        int liHash                  = item.hashCode();
		int liIndex                 = (liHash & 0x7FFFFFFF) % lTable.length;

		for (SymbolEntry e = lTable[liIndex], prev=null ; e != null; prev = e, e = e.getNext()) 
			{
            if ( e.hashCode() == liHash && e.equals(item) )
                {
                fHash_ModCount++;
                if (prev != null) 
                    {
                    prev.setNext(e.getNext());
                    } 
                else 
                    {
                    lTable[liIndex] = e.getNext();
                    }
                fHash_No--;
                return e;
                }/*if*/
			}/*for*/
		return null;
    }
	/**
	 *
	 */
    public final SymbolEntry	Hashtable_FindUnique(Symbol item)
    {
		if (item == null) 
			{ return null;}

		SymbolEntry lTable[]    = fHash;
        int liHash              = item.hashCode();
		int liIndex             = (liHash & 0x7FFFFFFF) % lTable.length;

		for (SymbolEntry e = lTable[liIndex] ; e != null ; e = e.getNext()) 
			{
            if ( e.hashCode() == liHash && e.equals(item) )
				{
				return e;
				}
			}/*for*/
		return null;
    }

	/**
	 *  Add all symbols in all index-pages to the hashtable 
	 */
    public final void Hashtable_Pages_Add()
    {
        Hashtable_Pages_Add(fPages);
    }

	/**
	 *  Add all symbols in all index-pages to the hashtable 
	 *
	 */
     final void Hashtable_Pages_Add(SymbolPageReference table[])
    {
        SymbolEntry  lHashTable[]       = fHash;

        /* add enough items to hashtable to avoid repeated rehash */
        Hashtable_Rehash(fSymbols_Max);
        int liHashtableLength   = fHash.length;

        /* Go through pages */
        int liLength  = fPages.length;
        for(int i= 0; i < liLength; i++)
			{
            for(SymbolPageReference e = table[i]; e != null; e = e.getNext()) 
                {
                SymbolPage  lPage   = e.getPage();

                /* handle only positive pages */
                if(lPage.fBase < 0)
                    continue;

                int liNoItems       = SYMBOL_SIZE;
                for(int j=0 ; j< liNoItems ; j++)
                    {
                    Symbol  lSymbol = lPage.fItems[j];
                    if( lSymbol != null )
                        {
                        int liIndex = (lSymbol.hashCode() & 0x7FFFFFFF) % liHashtableLength;

                        int liIdx = 0;/*.TODO fix */
                        fHash[liIndex] = new SymbolEntry(liIdx,lSymbol,fHash[liIndex]);
                        fHash_ModCount++;
                        fHash_No++;
                        }/*if Symbol exists*/
                    }/*for*/
                }/*for Hashpages*/
			}/*for pages*/
    }


    /*==============================================================*/
    /*  Paged Access                                                */
    /*==============================================================*/
    /**
     *
     */
	 final int	Pages_No(){
		return fPages_No;
    }

    /**
     *
     */
     final void Pages_Rehash() 
    {
        Pages_Rehash(fPages.length*2+1);
    }

     final void Pages_Rehash(int newCapacity) 
    {
		int 			    liOldLength = fPages.length;
		SymbolPageReference oldMap[] 	= fPages;
		SymbolPageReference newMap[]	= new SymbolPageReference[newCapacity];
		
		fPages_ModCount++;
		fPages_Threshold = (int)(newCapacity * fPages_LoadFactor);
		fPages = newMap;
		
		for (int i = liOldLength ; i-- > 0 ;) 
			{
			for (SymbolPageReference old = oldMap[i] ; old != null ; ) 
				{
				SymbolPageReference e = old;
				old = old.getNext();
				
				int index = (e.getPosition() & 0x7FFFFFFF) % newCapacity;
				e.setNext(newMap[index]);
				newMap[index] = e;
				}/*for*/
			}/*for*/
    }


	/**
	 *
	 */
     final SymbolPage	Page_Add(int index, SymbolPage page)
    {
		if (page == null) 
			{
			throw new NullPointerException();
			}
		
		/* Find in table */
		SymbolPageReference lTable[] = fPages;
		int	liPos 	= index & SYMBOLPAGE_MASK;
		int liIndex = (liPos & 0x7FFFFFFF) % lTable.length;
		for (SymbolPageReference lPageRef = lTable[liIndex] ; 
			 lPageRef != null ; 
			 lPageRef = lPageRef.getNext()) 
			{
			if (lPageRef.getPosition() == liPos) 
				{/* already exists */
				return lPageRef.getPage();
				}
			}/*for*/

		fPages_ModCount++;
		/* Check if need to rehash */
		if (fPages_No >= fPages_Threshold) 
			{
			Pages_Rehash();
			
			lTable	= fPages;
            liIndex = (liPos & 0x7FFFFFFF) % lTable.length;
			} 
		
		/*-- Add to table --*/
        SymbolPageReference   lRef = new SymbolPageReference(liPos,page,lTable[liIndex]);
		lTable[liIndex] = lRef;
		fPages_No++;
		return page;
    }

    /**
     *
     */
     final SymbolPage	Page_Add(int index)
    {
			
		return Page_Add(index, new SymbolPage(index));
    }
	/**
	 *
	 */
     final SymbolPage	Page_Find(int index)
    {
		SymbolPageReference lTable[] = fPages;
		int	liPos 	= index & SYMBOLPAGE_MASK;
        if(liPos == 0)
            {
            /**.TODO add shortcut to 0-index */
            }
		int liIndex = (liPos & 0x7FFFFFFF) % lTable.length;

		for (SymbolPageReference e = lTable[liIndex] ; e != null ; e = e.getNext()) 
			{
			if( e.getPosition()== liPos)
				{
				return e.getPage();
				}
			}
		return null;
    }
    /**
     *
     */
     final SymbolPage	Page_Remove(int index)
    {
		SymbolPageReference lTable[] = fPages;
		int	liPos 	= index & SYMBOLPAGE_MASK;
		int liIndex = (liPos & 0x7FFFFFFF) % lTable.length;

		for (SymbolPageReference e = lTable[liIndex], prev=null ; e != null; prev = e, e = e.getNext()) 
			{
            if ( e.getPosition() == liPos ) 
                {
                fPages_ModCount++;
                if (prev != null) 
                    {
                    prev.setNext(e.getNext());
                    } 
                else 
                    {
                    lTable[liIndex] = e.getNext();
                    }
                fPages_No--;
                return e.getPage();
                }/*if*/
			}/*for*/
		return null;
    }


    /*==============================================================*/
    /*  Symbol-management                                           */
    /*==============================================================*/

    /**
     *  Retreive PoolItem by index
     */
    public final Symbol     Symbol_Get(int index)
    {
		SymbolPage    lPage = Page_Find(index);
        if(lPage == null )
            {
            return null;
            }
        return lPage.getItem(index);
    }   
 
    /**
     *  Sets Item at specified index 
     */
    public final Symbol     Symbol_Set(int index, Symbol item)
    {
        Symbol    lItem;
		if (item == null) 
			{
            /* Is page there or not */
            SymbolPage    lPage = Page_Find(index);
            if(lPage != null )
                {
                lPage.setItem(index, null);
                }/*if*/
            return null;
            }
        
        /* Is page there or not */
		SymbolPage    lPage = Page_Find(index);
        if(lPage == null )
            {
            lPage = Page_Add(index);
            if(lPage == null )
                {throw new NullPointerException();}
            }/*if*/

        /* get item from page */
        lItem = lPage.getItem(index);
        if(lItem != null )
            {
            }/*if*/

        /* Finally add to page */
        lPage.setItem(index, item);

        fSymbols_Max  = index >= fSymbols_Max ? index : fSymbols_Max;
		return item;
    }

    /**
     *  Sets Item at specified index 
     */
    public final Symbol     Symbol_Set(int index, Data data)
    {
        return Symbol_Set(index, new Symbol(data));
    }
    /**
     *  Sets Item at specified index unless uniqueness is required and the itemis already in hashtable.
     */
    public final Symbol  Symbol_SetUnique(int index, Symbol item)
    {
        Symbol        lItem;
        SymbolEntry   lEntry;

		if (item == null) 
			{
            /* Is page there or not */
            SymbolPage    lPage = Page_Find(index);
            if(lPage != null )
                {
                lPage.setItem(index, null);
                }/*if*/
            return null;
            }

        /* If unique and in hashtable then dont add it */
        lEntry = Hashtable_FindUnique(item);
        if( lEntry != null )
            return item;
        
        /* Is page there or not */
		SymbolPage    lPage = Page_Find(index);
        if(lPage == null )
            {
            lPage = Page_Add(index);
            if(lPage == null )
                {throw new NullPointerException();}
            }/*if*/

        /* get item from page */
        lItem = lPage.getItem(index);
        if(lItem != null )
            {
            lEntry = Hashtable_Remove(lItem);
            if(lEntry == null )
                {throw new NullPointerException();}
            }/*if*/

        /* Add to unique hashtable */
        lEntry = Hashtable_Add(index, item, false);
        if(lEntry == null )
            {throw new NullPointerException();}

        /* Finally add to page */
        lPage.setItem(index, item);

        fSymbols_Max  = index >= fSymbols_Max ? index : fSymbols_Max;

		return item;
    }
    /**
     *  Sets Item at specified index unless uniqueness is required and the itemis already in hashtable.
     */
    public final Symbol  Symbol_SetUnique(int index, Data data)
    {
        /*.TODO fix */
        return null;
    }

    /**
     * Add new Unique symbol if it does not exist. If it already exists then the old Symbol is bound to the input reference and returned. 
     *
     * @param reference Symbol to add. On exit it contains the bound Symbol. If the symbol already existed then it contains the old symbol and its index.
     * @return TRUE if the symbol was added. FALSE if the symbol already existed .
     * @throws BMLException if binding fails, usually due to memory problems.
     */
    public final boolean  Symbol_AddUnique(SymbolRef reference) throws BMLException{

        Symbol  lNew = reference.fBoundSymbol;

		SymbolEntry	lFound = Hashtable_FindUnique(lNew);
		if( lFound != null )
            {/*already in table*/
			reference.fRef	       = lFound.getIndex();
            reference.fBoundSymbol = lFound.getSymbol();
			return false;
            }

		/*  ADD to hashtable and pages */
        int liIndex        	= fSymbols_Max+1;
        SymbolEntry lAdded 	= Hashtable_Add(liIndex, lNew, true);

		Symbol  	lPAdded	= Symbol_Set(liIndex,lNew);
		if( lPAdded == null )
			{
            throw new BMLException("Failed to set symbol table entry:'"+liIndex+"' for symbol: '"+lNew+"'");
			}

		reference.fRef 		    = liIndex;
        return true;
    }

}


/*.IEnd,SymbolTable,====================================*/
