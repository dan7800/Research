package st.ata.util;

import java.io.IOException;

// Tested by : TestTextPool.java

/**
 * TextPool is a buffer that is meant to hold data for Text objects.
 * Since Text objects do not store the data themselves there should be
 * some other object that actually holds the buffers for them. TextPool
 * meets that requirements.
 */

public final class TextPool {
    // At any given time mAvailable + mNext == mBuffer.length

    private int mAvailable;     // Number bytes available in mBuffer
    private int mNext;          // Index into the next free byte in buffer.

    private int mBufferSize;    // Default size for the buffer

    private byte[] mBuffer;     // The buffer itself

    // True if there is an active TextPool.OutputStream object 

    private boolean mStreamOperation = false;  

    /**
     *  Construct a TextPool object with aBufferSize as the default buffer size
     *  @param aBufferSize  The default buffer size. If a single Text object
     *  in the pool needs to be larger than this size, a larger buffer will be used
     *  for that object. All buffers in the TextPool are guarenteed to be
     *  at least aBufferSize bytes long.
     */

    public TextPool(int aBufferSize) {
        X.checkargs(aBufferSize > 0);
        mBuffer = new byte[aBufferSize];
        mAvailable = aBufferSize;
        mNext = 0;
        mBufferSize = aBufferSize;
    }

    /**
     *  Construct a TextPool object with 8 K buffer
     *  Equivalent to TextPool(8012);
     */
    public TextPool() {
        this(8 * 1024);
    }

    /**
     *  Return a TextPool.OutputStream so that the client can
     *  create a Text object by incrementally supplying the data.
     *  Once a TextPool.OutputStream object is obtained using this
     *  method, copy can not be attempted on this TextPool object
     *  until close method is called on the TextPool.OutputStream.
     *  At any given time there can be at most one active
     *  TextPool.OutputStream.
     *  @param aHintSize    The client can indicate a size for
     *  the text object that will be constructed using the new
     *  TextPool.OutputStream. If the client has no estimate of
     *  the size it may pass 0. The implementation tries to optimize
     *  in case Text size is indeed of the size given by aHintSize.
     *  The same TextPool.OutputStream object can be used to
     *  build several Text objects. The size will be useful only for
     *  the first Text object built.
     */

    public TextPool.OutputStream getOutputStream(int aHintSize) {
        if(aHintSize > 0 && mAvailable < aHintSize)
            realloc(aHintSize);
        return new TextPool.OutputStream();
    }

    /** Resets the allocation pointer to re-use buffer space.  Will
     *  start trashing Text objects previously returned. */
    public void reset() {
        X.check(!mStreamOperation);
        mAvailable = mBuffer.length;
        mNext = 0;
    }

    /**
     *  realloc allocates a new buffer that is at least aNewSize bytes
     *  long and assigns to mBuffer.
     *  It also resets values for mAvailable and mNext.
     *  @param aNewSize the minimum number of bytes that the new buffer
     *  must have.
     */
    private void realloc(int aNewSize) {
        int bufSize = mBufferSize;
        while(bufSize < aNewSize)
            bufSize *= 2;

        mBuffer = new byte[bufSize];
        mAvailable = bufSize;
        mNext = 0;
    }

    /**
     *  Creates a new Text object whose data is stored in this TextPool.
     *  This method allocates memory out of the current buffer, if
     *  possible. Otherwise a new buffer is created and space allocated.
     */

    public Text copy(Text aText) {
        X.check(!mStreamOperation);

        int length = aText.length();
        if(mAvailable < length)
            realloc(length);

        System.arraycopy(aText.buf, aText.start, mBuffer, mNext, length);

        Text t = new Text(mBuffer, mNext, mNext+length);
        mNext += length;
        mAvailable -= length;

        return t;
    }

    /**
     *  Creates a new Text object whose data is stored in this TextPool.
     *  The contents of the given String is assumed to be ASCII. If they are
     *  not the high order bytes are truncated.
     *  This method allocates memory out of the current buffer, if
     *  possible. Otherwise a new buffer is created and space allocated.
     */

    public Text copy(String str) {
        X.check(!mStreamOperation);

        int length = str.length();

        if(mAvailable < length)
            realloc(length);

        for(int i = 0; i < length; ++i)
            mBuffer[mNext + i] = (byte)str.charAt(i);

        Text t = new Text(mBuffer, mNext, mNext+length);
        mNext += length;
        mAvailable -= length;

        return t;
    }
    /**
     *  TextPool.OutPutStream is a mechanism that allows clients to
     *  create a new Text object by supplying the data incrementally.
     *  All data that is written using various write methods until
     *  done is called are accumulated. done() returns the Text object
     *  created. After done is called the client can use the various
     *  write methods again to create a new Text object. When no more 
     *  Text objects are required this way, the client can call
     *  close method. done() and close() can be combined using doneAndClose()
     *  method.
     */

    public class OutputStream extends java.io.OutputStream {
        private int mStart;

        /**
         *  Construct a TextPool.OutputStream object. Asserts that there
         *  is no other active TextPollOutputStream on the same TextPool.
         */

        OutputStream() {
            X.check(!mStreamOperation);
            mStart = mNext;
            mStreamOperation = true;
        }

        /**
         *  Allocate a buffer with more memory.
         *  @param aCnt the number of additional bytes required.
         */
        private void more(int aCnt) {
            byte[] oldBuf = mBuffer;
            int toSave = mNext - mStart;
            realloc(toSave + aCnt);
            if(toSave > 0) {
                System.arraycopy(oldBuf, mStart, mBuffer, 0, toSave);
                mNext = toSave;
                mAvailable -= toSave;
            }
            mStart = 0;
        }

        /**
         *  Write the given byte into the OutputStream.
         */

        public void write(int b) throws IOException {
            if(mAvailable < 1)
                more(1);
            mBuffer[mNext++] = (byte)b;
            --mAvailable;
        }

        /**
         *  Write the given part of the buffer into the OutputStream
         */

        public void write(byte[] aBuf, int aStart, int aLen)
            throws IOException {
            if(mAvailable < aLen)
                more(aLen);
            System.arraycopy(aBuf, aStart, mBuffer, mNext, aLen);
            mNext += aLen;
            mAvailable -= aLen;
        }

        /**
         *  Write the given Text into the TextPool.OutputStream
         */
        public void write(Text aText) throws IOException {
            write(aText.buf, aText.start, aText.length());
        }
        /**
         *  Signal the end of builing the text object.
         *  @return A Text object that is been just built.
         *  The client can call write methods to build a new object.
         */

        public Text done() throws IOException {
            Text t = new Text(mBuffer, mStart, mNext);
            mStart = mNext;
            return t;
        }

        /**
         *  Close the TextPool.OutputStream object. The client can not
         *  build any more Text objects using this TextPool.OutputStream
         *  object after calling this method.
         */

        public void close() throws IOException {
            mStart = -1;        // Signal this it's closed
            mStreamOperation = false;
        }

        /**
         *  Equivalent to <code>
         *  {
         *      Text t = done();
         *      close();
         *      return t;
         *  }
         *  </code>
         *  but marginally efficient.
         */
        public Text doneAndClose() throws IOException {
            Text t = new Text(mBuffer, mStart, mNext);
            mStart = -1;        // Signal this it's closed
            mStreamOperation = false;
            return t;
        }

        /**
         *  Ensure that this object is closed.
         */
        protected void finalize() {
            if(mStart >= 0)                 // Not closed.
                mStreamOperation = false;
        }
    };
};
