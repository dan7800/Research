package net.sf.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Just a pipe, for now.
 *
 * <p>
 *
 * It may be a good idea to utilize the JDK 1.2 weak references to utilize
 * the buffer pool.
 *
 * @author Copyright &copy; <a href="mailto:vt@freehold.crocodile.org">Vadim Tkachenko</a> 1995-1999
 * @version $Id: Pipe.java,v 1.2 2007-06-14 04:32:07 vtt Exp $
 */
public class Pipe {

    /**
     * Buffer size limit.
     *
     * No buffer created by this class will exceed this size.
     */
    public static final	int MAX_BUFFER_SIZE = 64*1024;

    /**
     * Pipe the input stream to the output stream.
     *
     * Used to pipe the data content stream through the digest stream.
     *
     * <p>
     *
     * Note that this method doesn't consider the incomplete read as an
     * error. It's useful when you, for example, want to calculate the
     * digest for incoming stream, and there's no guarantee that the stream
     * will be complete, but the incomplete stream has non-zero value
     * because, for instance, there's a <code>REGET</code> method somewhere
     * else.
     *
     * @param in Stream to read from.
     *
     * @param out Stream to write to.
     *
     * @param contentLength Bytes to pipe.
     *
     * @return bytes left to read.
     *
     * @exception IOException if there's a problem.
     */
    public static long pipe(InputStream in, OutputStream out, long contentLength) throws IOException {

        if (contentLength <= 0) {

            return 0;
        }

     	//long total = contentLength;
     	int bufSize = (int)((contentLength < MAX_BUFFER_SIZE)
     			? contentLength
     			: MAX_BUFFER_SIZE);
     	byte buffer[] = new byte[bufSize];
     	int readSize = bufSize;	// That's OK, we just made sure that
     				// the buffer is no longer than
     				// the content length
     	int bytesRead = 0;

     	while ( contentLength > 0 ) {

     	    bytesRead = in.read( buffer,0,readSize );

     	    if ( bytesRead == -1 ) {

     	        break;
     	    }

     	    contentLength -= bytesRead;
     	    readSize = (contentLength < bufSize)
     	    		? (int)contentLength
     	    		: bufSize;

     	    if ( out != null ) {

     	        out.write(buffer,0,bytesRead);
     	    }
     	}

     	if ( contentLength != 0 ) {

     	    //complain( null,LOG_WARNING,"pipe","incomplete read: "+contentLength+" bytes due" );
     	}

     	if ( out != null ) {

     	    out.flush();
     	}

    	return contentLength;
    }
}
