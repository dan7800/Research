/*-*- Mode: java; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 0 -*-*/
/*.IH,MessageBlockOutputStream,======================================*/
/*.IC,--- COPYRIGHT (c) --  Open ebXML - 2001 ---

     The contents of this file are subject to the Open ebXML Public License
     Version 1.0 (the "License"); you may not use this file except in
     compliance with the License. You may obtain a copy of the License at
     'http://www.openebxml.org/LICENSE/OpenebXML-LICENSE-1.0.txt'

     Software distributed under the License is distributed on an "AS IS"
     basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
     License for the specific language governing rights and limitations
     under the License.

     The Initial Developer of the Original Code is Anders W. Tell.
     Portions created by Financial Toolsmiths AB are Copyright (C) 
     Financial Toolsmiths AB 1993-2001. All Rights Reserved.

     Contributor(s): see author tag.

---------------------------------------------------------------------*/
/*.IA,	PUBLIC Include File MessageBlockOutputStream.java			*/
package org.openebxml.comp.util;


/************************************************
	Includes
\************************************************/
import java.io.IOException;           	/* JME CLDC 1.0 */
import java.io.OutputStream;           	/* JME CLDC 1.0 */
import java.lang.Integer;           	/* JME CLDC 1.0 */

/**
 *  Wrapper class to be used when writing data to a list of MessageBlocks.
 *
 * @author Anders W. Tell   Financial Toolsmiths AB
 * @version $Id: MessageBlockOutputStream.java,v 1.2 2001/10/21 09:47:52 awtopensource Exp $
 */

public class MessageBlockOutputStream  extends OutputStream {

    /****/
    MessageBlockPool    fPool;

	/****/
	MessageBlock 	    fBlock;

	/****/
	MessageBlock 	    fCurrent;

    /**
     */
    public MessageBlockOutputStream(MessageBlock blk)
    {
        fPool       = null;
		fBlock		= blk;
		fCurrent	= blk;
    }

    /**
     */
    public MessageBlockOutputStream(MessageBlockPool pool, MessageBlock blk)
    {
        fPool       = pool;
		fBlock		= blk;
		fCurrent	= blk;
    }

	/**
     * Writes <code>b.length</code> bytes from the specified byte array 
     * to this output stream. The general contract for <code>write(b)</code> 
     * is that it should have exactly the same effect as the call 
     * <code>write(b, 0, b.length)</code>.
     *
     * @param      b   the data.
     * @exception  IOException  if an I/O error occurs.
     */
    public final void write(int bt) 
    throws IOException
    {
        if( fCurrent.getSpace() > 0 )
            {
            fCurrent.WriteOne(bt);
            return;
            }
        MessageBlock    lNew;
        if(fPool != null )
            {
            lNew = fPool.makeMessageBlock();
            }
        else
            {
            lNew = new MessageBlock();
            }
        fCurrent.setContinued(lNew);
        fCurrent = lNew; 
        fCurrent.WriteOne(bt);
    }

    /**
     * Writes <code>len</code> bytes from the specified byte array 
     * starting at offset <code>off</code> to this output stream. 
     * The general contract for <code>write(b, off, len)</code> is that 
     * some of the bytes in the array <code>b</code> are written to the 
     * output stream in order; element <code>b[off]</code> is the first 
     * byte written and <code>b[off+len-1]</code> is the last byte written 
     * by this operation.
     * <p>
     * The <code>write</code> method of <code>OutputStream</code> calls 
     * the write method of one argument on each of the bytes to be 
     * written out. Subclasses are encouraged to override this method and 
     * provide a more efficient implementation. 
     * <p>
     * If <code>b</code> is <code>null</code>, a 
     * <code>NullPointerException</code> is thrown.
     * <p>
     * If <code>off</code> is negative, or <code>len</code> is negative, or 
     * <code>off+len</code> is greater than the length of the array 
     * <code>b</code>, then an <tt>IndexOutOfBoundsException</tt> is thrown.
     *
     * @param      b     the data.
     * @param      off   the start offset in the data.
     * @param      len   the number of bytes to write.
     * @exception  IOException  if an I/O error occurs. In particular, 
     *             an <code>IOException</code> is thrown if the output 
     *             stream is closed.
     */
    public synchronized void write(byte buffer[], int offset, int len) 
	throws IOException
    {
        if (buffer == null) 
            {
            throw new NullPointerException();
            } 
        else if ((offset < 0) 
                 || (offset > buffer.length) 
                 || (len < 0) 
                 || ((offset + len) > buffer.length) 
                 || ((offset + len) < 0)) 
            {
            throw new IndexOutOfBoundsException();
            } 
        else if (len == 0) 
            {
            return;
            }

        int liPos       = offset;
		int	liCount 	= len;
		while(0 < liCount )
			{
            int liSpace = fCurrent.getSpace();
			if( 0 < liSpace)
				{
                liSpace = liSpace > liCount? liCount: liSpace;
				System.arraycopy(buffer, liPos,
                                 fCurrent.getData(),fCurrent.getWritten()+1,
								 liSpace);

				liCount -= liSpace;
				liPos 	+= liSpace;
				fCurrent.addWritten(liSpace);
				}
			else
				{
                MessageBlock    lNew;
                if(fPool != null )
                    {
                    lNew = fPool.makeMessageBlock();
                    }
                else
                    {
                    lNew = new MessageBlock();
                    }
                fCurrent.setContinued(lNew);
                fCurrent = lNew; 
				}
			}/*while*/
	}

	/****/
    public synchronized void flush() throws IOException
    {   
        /* Do nothing */
	}    


	/****/
	public void close() throws IOException 
    {
        /* Do nothing */
	}
}


/*.IEnd,MessageBlockOutputStream,====================================*/
