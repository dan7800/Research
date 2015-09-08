/*-*- Mode: java; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 0 -*-*/
/*.IH,MessageBlock,======================================*/
/*.IC,--- COPYRIGHT (c) --  Open ebXML - 2001 ---

     The contents of this file are subject to the Open ebXML Public License
     Version 1.0 (the "License"); you may not use this file except in
     compliance with the License. You may obtain a copy of the License at
     'http://www.openebxml.org/LICENSE/OpenebXML-LICENSE-1.0.txt'

     Software distributed under the License is distributed on an "AS IS"
     basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
     License for the specific language governing rights and limitations
     under the License.

     The Initial Developer of the Original Code is Prashant Jain

     Portions created by Washington University have Copyright (C)
     <P>
     Java ACE is copyrighted by  <A HREF="http://www.cs.wustl.edu/~schmidt">Douglas C. Schmidt</A> and his research group at <A HREF="http://www.wustl.edu">Washington University</A>.You are free to do anything you like with the Java ACE source code such as including it in commercial software, as long as you include this copyright statement along with code built using Java ACE. 
</P>

     Portions created by Financial Toolsmiths AB are Copyright (C) 
     Financial Toolsmiths AB 1993-2001. All Rights Reserved.

     Contributor(s): see author tag.

---------------------------------------------------------------------*/
/*.IA,	PUBLIC Include File MessageBlock.java			*/
package org.openebxml.comp.util;


/************************************************
	Includes
\************************************************/
import java.lang.IndexOutOfBoundsException;           /* JME CLDC 1.0 */

/**
 *  Container for a fixed number of bytes that may be used form a linked stream of bytes.
 *
 * @author Anders W. Tell   Financial Toolsmiths AB
 * @version $Id: MessageBlock.java,v 1.8 2002/06/16 09:58:58 awtopensource Exp $
 */

public final class MessageBlock  {

    /****/
    public final static int TYPE_DATA   = 0;

    /****/
    public final static int TYPE_CLOSE   = 1;

    /****/
    public final static int TYPE_FLUSH   = 10;

    /****/
    public final static int PRIORITY_LOW      	= -255;

    /****/
    public final static int PRIORITY_NORMAL   	= 0;

    /****/
    public final static int PRIORITY_HIGH   	= 255;

    /****/
	public static final int		DEFAULT_SIZE	= 512;/*.TODO increase to 512*/


    /**
     * Type of block.
     *
     */
    int             fType;
	
	/**
     *  Priority
     */
	int 			fPriority;

	/**
     *  Actual data in Block
     */
	byte[]          fData;

	/**
     * The number of read bytes from this block
     */
	int             fRead;
	
	/**
     * The number of bytes written to this block
     */
	int             fWritten;

	/**
     * Single linked list of MessageBlocks
     */
	MessageBlock    fContinued;

	/**
     *   Used in linking messages into Queues or lists.
     */
	MessageBlock    fNext;

	/**
     *   Used in linking messages into Queues or lists.
     */
	MessageBlock    fPrev;
	
	
	/**
     * Default Constructor
     */
    public MessageBlock()
    {
        this(DEFAULT_SIZE);
    }

	/**
     * Allocates Data of specified 'size'.
     *
     * @param size must be greater than zero.
     */
    public MessageBlock(int size)
    {
		fType		= TYPE_DATA;
		fPriority	= PRIORITY_NORMAL;
		fData		= new byte[size];
		fRead		= -1;
		fWritten	= -1;
		fNext		= null;
		fPrev		= null;
		fContinued	= null;
    }

	/**
     * Shallow copy contructor.
     *
     * @param type Type of Block.
     * @param priority Priority.
     * @param written How many bytes that should be assumed as written.
     * @param read How many bytes that should be assumed as read. MUST always be less or equal to 'written'.
     * @param data Byte-array to use as data storage.
     */
    public MessageBlock(int type, int prio, int written, int read,byte[] data)
    {
		fType		= type;
		fPriority	= prio;
		fData		= data;
		fRead		= read -1;
		fWritten	= written -1;
		fNext		= null;
		fPrev		= null;
		fContinued	= null;
    }

	/**
     * Uses previously allocated data. Assumes that no information has been written to or read from the data.
     *
     * @param data MUST not be null.
     */
    public MessageBlock(byte[] data)
    {
		fType		= TYPE_DATA;
		fPriority	= PRIORITY_NORMAL;
		fData		= data;
		fRead		= -1;
		fWritten	= -1;
		fNext		= null;
		fPrev		= null;
		fContinued	= null;
    }

	/**
	 * Get the value of Type.
	 * @return value of Type.
	 */
	public final int getType() {
		return fType;
	}
	
	/**
	 * Set the value of Type.
	 * @param v  Value to assign to Type.
	 */
	public final void setType(int  v) {
		this.fType = v;
	}
	
	/**
	 * Get the value of Priority.
	 * @return value of Priority.
	 */
	public final int getPriority() {
		return fPriority;
	}
	
	/**
	 * Set the value of Priority.
	 * @param v  Value to assign to Priority.
	 */
	public final void setPriority(int  v) {
		this.fPriority = v;
	}
	

	/**
	 * Get the value of Data.
	 * @return value of Data.
	 */
	public final byte[] getData() {
		return fData;
	}
	
	/**
	 * Set the value of Data.
	 * @param v  Value to assign to Data.
	 */
	public final void setData(byte[]  v) {
		this.fData = v;
	}

	/**
	 * Get length of Data
	 * @return length of Data.
	 */
	public final int getLength() {
		return fWritten - fRead;
	}
	
	/**
	 * Get length of byte buffer.
	 * @return space in byte buffer
	 */
	public final int getSize() {
		return fData.length;
	}
	
	/**
	 * Get space left to write.
	 * @return space left
	 */
	public final int getSpace() {
		return fData.length - fWritten - 1;
	}
	
	/**
	 * Get length of all Blocks in continued list.
	 * @return length of Data.
	 */
	public final int getContinuedLength() 
    {
        int liLength = 0;
        for (MessageBlock lCurrent = this;
             lCurrent !=null;
             lCurrent = lCurrent.getContinued())
            {
            liLength += lCurrent.getLength();
            }/*for*/
        
		return liLength;
	}

	/**
     * Calculate total length along linked list and along continued list.
	 * @return length of Data.
     */
    public final int getMessageLength()
    {
        int             liCount = 0;
		MessageBlock    lCurr   = this;

        while( lCurr != null )
            {
            liCount += lCurr.getContinuedLength();
            lCurr   = lCurr.getNext();
            }/*while*/
        return liCount;
    }
	
	
    /**
     * Get the value of Read.
     * @return value of Read.
     */
	public final int getRead() {
		return fRead;
	}
	
    /**
     * Set the value of Read.
     * @param v  Value to assign to Read.
     */
	public final void setRead(int  v) {
		this.fRead = v;
	}
    /**
     * Read one byte from this Block assuming there are any to read ie getLength() > 0.
     * 
     */
	public final byte ReadOne() 
    {
        fRead++;
		return fData[fRead];
	}
    /**
     * Return one byte previously read by ReadOne()
     * 
     */
	public final void UnReadOne(byte ch) 
    {
		fData[fRead] = ch;
        fRead--;
	}
    /**
     * Adds to the value of Read.
     * @param v  Value to assign to Read.
     */
	public final int addRead(int  v) {
		fRead += v;
        return fRead;
	}

	
    /**
     * Get the value of Written.
     * @return value of Written.
     */
	public final int getWritten() {
		return fWritten;
	}
	
    /**
     * Set the value of Written.
     * @param v  Value to assign to Written.
     */
	public final void setWritten(int  v) {
		this.fWritten = v;
	}
	
    /**
     * Adds to the value of Written.
     * @param v  Value to assign to Written.
     */
	public final int addWritten(int  v) {
		fWritten += v;
        return fWritten;
	}
	
    /****/
    public final void WriteOne(byte bt)
    {
        fWritten++;
        fData[fWritten] = bt;
    }
    /****/
    public final void WriteOne(int bt)
    {
        fWritten++;
        fData[fWritten] = (byte)bt;
    }

    /**
     * Write many bytes to Block. Only bytes that fits into remaining space are written.
     * @param bt Byte array to read from.
     * @param offset where to start read from in supplied byte array.
     * @param length number of bytes to write.
     * @return number of exess bytes not written.
     */
    public final int Write(byte[] bt, int offset ,int length)
    {
        int liSpace   = getSpace();
        int liToWrite = liSpace > length ? length : liSpace;

        System.arraycopy(bt, offset, fData, fWritten+1, liToWrite);
        fWritten    += liToWrite;

        return  length > liToWrite ? length - liToWrite : 0 ;
    }
	
    /**
     * Get the value of Next.
     * @return value of Next.
     */
	public final MessageBlock getNext() {
		return fNext;
	}
	
    /**
     * Set the value of Next.
     * @param v  Value to assign to Next.
     */
	public final void setNext(MessageBlock  v) {
		this.fNext = v;
	}
	
	
    /**
     * Get the value of Prev.
     * @return value of Prev.
     */
	public final MessageBlock getPrev() {
		return fPrev;
	}
	
    /**
     * Set the value of Prev.
     * @param v  Value to assign to Prev.
     */
	public final void setPrev(MessageBlock  v) {
		this.fPrev = v;
	}

	
    /**
     * Get the value of Continued.
     * @return value of Continued.
     */
	public final MessageBlock getContinued() {
		return fContinued;
	}
	
    /**
     * Set the value of Continued.
     * @param v  Value to assign to Continued.
     */
	public final void setContinued(MessageBlock  v) {
		this.fContinued = v;
	}


	/**
     *  Resets use of this Block
     */
    public final void Reset()
    {
		fType		= TYPE_DATA;
		fPriority	= PRIORITY_NORMAL;
		fRead		= -1;
		fWritten	= -1;
		fNext		= null;
		fPrev		= null;
		fContinued	= null;
    }


    /**
     * Clones the MessageBlock along Continuation links.
     *
     * @param shallow TRUE if only Block header should be copied and not its data, FALSE if Data array to should be copied.
     * @return New MessageBlocks linked with continuation link
     */
    public final MessageBlock cloneContinued(boolean shallow)
    {
        MessageBlock lFirst = null;;
        MessageBlock lLast  = null;
        MessageBlock lCurr  = this;

        while(lCurr != null )
            {
            byte[] lData = lCurr.fData;
            if( shallow && (lData != null))
                {
                int liLength    = lCurr.fData.length;
                lData           = new byte[liLength];
                System.arraycopy(fData, 0, lData, 0, liLength);
                }

            MessageBlock lNew = new MessageBlock(lCurr.fType,
                                                 lCurr.fPriority,
                                                 lCurr.fWritten,
                                                 lCurr.fRead,
                                                 lData);
            if( lLast != null )
                {
                lLast.setContinued(lNew);
                }
            else
                {
                lFirst = lNew;
                }
            lLast = lNew;
            
            lCurr = lCurr.getContinued();
            }/*while*/

        return lFirst;
    }

    /**
     * Clones the MessageBlock along Continuation links.
     *
     * @param shallow TRUE if only Block header should be copied and not its data, FALSE if Data array to should be copied.
     * @return New MessageBlocks linked with continuation link
     */
    public final MessageBlock cloneContinued(MessageBlockPool pool, 
                                             boolean shallow)
    {
        MessageBlock lFirst = null;;
        MessageBlock lLast  = null;
        MessageBlock lCurr  = this;

        while(lCurr != null )
            {
            byte[] lData = lCurr.fData;
            if( shallow && (lData != null))
                {
                int liLength    = lCurr.fData.length;
                lData           = new byte[liLength];
                System.arraycopy(fData, 0, lData, 0, liLength);
                }

            /*.TODO allocate from Pool */
            MessageBlock lNew = new MessageBlock(lCurr.fType,
                                                 lCurr.fPriority,
                                                 lCurr.fWritten,
                                                 lCurr.fRead,
                                                 lData);
            if( lLast != null )
                {
                lLast.setContinued(lNew);
                }
            else
                {
                lFirst = lNew;
                }
            lLast = lNew;
            
            lCurr = lCurr.getContinued();
            }/*while*/

        return lFirst;
    }

    /**
     *  Moves all bytes between Read and Written to beginning of buffer and
     *  changes reading and writing markers accordingly.
     */
    public final void Reposition()
    {
        int liLength = fWritten - fRead;
        
        if( liLength > 0)
            {
            System.arraycopy(fData, fRead+1, fData, 0, liLength);
            }
        fWritten = liLength-1;
        fRead    = -1;
    }
    /**
     * Splits the Block into two halfes while maintaining reading and writing markers. If OutOfBounds position is supplied then no split takes place. The new block has the same size as the splitted block.
     *
     * @param position Index where the split occurs
     * @param cont TRUE if new Block should be fitted into continuation chain
     * @return MessageBlock reference to Block after the split point. If no split has occured then NULL is returned.
     * @throws IndexOutOfBoundsException
     */
    public final MessageBlock Split(int position, boolean cont)
    throws IndexOutOfBoundsException
    {
        if(position < 0 || fData.length <= position)
            {
            throw new IndexOutOfBoundsException();
            }
        MessageBlock lNew = new MessageBlock(fData.length);

        int liToCopy = fData.length - position;
        System.arraycopy(fData, position, lNew.fData,0, liToCopy);

        /* Fix read and write marker */
        if(fWritten >= position)
            {
            lNew.fWritten   = fWritten - position;
            fWritten        = position-1;
            }

        if(fRead >= position)
            {
            lNew.fRead   = fRead - position;
            fRead        = position-1;
            }

        if(cont)
            {
            lNew.fContinued = this.fContinued;
            this.fContinued = lNew;
            }

        return lNew;
    }
    /**
     * Splits the Block into two halfes while maintaining reading and writing markers. If OutOfBounds position is supplied then no split takes place. The new block has the same size as the splitted block.
     *
     * @param position Index where the split occurs
     * @param cont TRUE if new Block should be fitted into continuation chain
     * @return MessageBlock reference to Block after the split point. If no split has occured then NULL is returned.
     * @throws IndexOutOfBoundsException
     */
    public final MessageBlock Split(MessageBlockPool pool, 
                                    int position, boolean cont)
    throws IndexOutOfBoundsException
    {
        if(position < 0 || fData.length <= position)
            {
            throw new IndexOutOfBoundsException();
            }

        /*.TODO allocate from Pool */
        MessageBlock lNew = new MessageBlock(fData.length);

        int liToCopy = fData.length - position;
        System.arraycopy(fData, position, lNew.fData,0, liToCopy);

        /* Fix read and write marker */
        if(fWritten >= position)
            {
            lNew.fWritten   = fWritten - position;
            fWritten        = position-1;
            }

        if(fRead >= position)
            {
            lNew.fRead   = fRead - position;
            fRead        = position-1;
            }

        if(cont)
            {
            lNew.fContinued = this.fContinued;
            this.fContinued = lNew;
            }

        return lNew;
    }
}


/*.IEnd,MessageBlock,====================================*/
