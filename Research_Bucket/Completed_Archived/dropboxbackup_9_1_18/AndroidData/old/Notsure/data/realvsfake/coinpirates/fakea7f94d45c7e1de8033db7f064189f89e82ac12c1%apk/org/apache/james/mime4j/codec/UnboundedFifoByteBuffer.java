package org.apache.james.mime4j.codec;

import java.util.Iterator;
import java.util.NoSuchElementException;

class UnboundedFifoByteBuffer
{
  protected byte[] buffer;
  protected int head;
  protected int tail;

  public UnboundedFifoByteBuffer()
  {
    this(32);
  }

  public UnboundedFifoByteBuffer(int paramInt)
  {
    if (paramInt <= 0)
      throw new IllegalArgumentException("The size must be greater than 0");
    this.buffer = new byte[paramInt + 1];
    this.head = 0;
    this.tail = 0;
  }

  private int decrement(int paramInt)
  {
    int i = paramInt - 1;
    if (i < 0)
      i = this.buffer.length - 1;
    return i;
  }

  private int increment(int paramInt)
  {
    int i = paramInt + 1;
    if (i >= this.buffer.length)
      i = 0;
    return i;
  }

  public boolean add(byte paramByte)
  {
    if (1 + size() >= this.buffer.length)
    {
      byte[] arrayOfByte = new byte[1 + 2 * (this.buffer.length - 1)];
      int i = 0;
      int j = this.head;
      while (j != this.tail)
      {
        arrayOfByte[i] = this.buffer[j];
        this.buffer[j] = 0;
        i++;
        j++;
        if (j == this.buffer.length)
          j = 0;
      }
      this.buffer = arrayOfByte;
      this.head = 0;
      this.tail = i;
    }
    this.buffer[this.tail] = paramByte;
    this.tail = (1 + this.tail);
    if (this.tail >= this.buffer.length)
      this.tail = 0;
    return true;
  }

  public byte get()
  {
    if (isEmpty())
      throw new IllegalStateException("The buffer is already empty");
    return this.buffer[this.head];
  }

  public boolean isEmpty()
  {
    return size() == 0;
  }

  public Iterator<Byte> iterator()
  {
    return new Iterator()
    {
      private int index = UnboundedFifoByteBuffer.this.head;
      private int lastReturnedIndex = -1;

      public boolean hasNext()
      {
        return this.index != UnboundedFifoByteBuffer.this.tail;
      }

      public Byte next()
      {
        if (!hasNext())
          throw new NoSuchElementException();
        this.lastReturnedIndex = this.index;
        this.index = UnboundedFifoByteBuffer.this.increment(this.index);
        return new Byte(UnboundedFifoByteBuffer.this.buffer[this.lastReturnedIndex]);
      }

      public void remove()
      {
        if (this.lastReturnedIndex == -1)
          throw new IllegalStateException();
        if (this.lastReturnedIndex == UnboundedFifoByteBuffer.this.head)
        {
          UnboundedFifoByteBuffer.this.remove();
          this.lastReturnedIndex = -1;
          return;
        }
        int i = 1 + this.lastReturnedIndex;
        while (i != UnboundedFifoByteBuffer.this.tail)
          if (i >= UnboundedFifoByteBuffer.this.buffer.length)
          {
            UnboundedFifoByteBuffer.this.buffer[(i - 1)] = UnboundedFifoByteBuffer.this.buffer[0];
            i = 0;
          }
          else
          {
            UnboundedFifoByteBuffer.this.buffer[(i - 1)] = UnboundedFifoByteBuffer.this.buffer[i];
            i++;
          }
        this.lastReturnedIndex = -1;
        UnboundedFifoByteBuffer.this.tail = UnboundedFifoByteBuffer.this.decrement(UnboundedFifoByteBuffer.this.tail);
        UnboundedFifoByteBuffer.this.buffer[UnboundedFifoByteBuffer.this.tail] = 0;
        this.index = UnboundedFifoByteBuffer.this.decrement(this.index);
      }
    };
  }

  public byte remove()
  {
    if (isEmpty())
      throw new IllegalStateException("The buffer is already empty");
    byte b = this.buffer[this.head];
    this.head = (1 + this.head);
    if (this.head >= this.buffer.length)
      this.head = 0;
    return b;
  }

  public int size()
  {
    if (this.tail < this.head)
      return this.buffer.length - this.head + this.tail;
    return this.tail - this.head;
  }
}

/* Location:
 * Qualified Name:     org.apache.james.mime4j.codec.UnboundedFifoByteBuffer
 * Java Class Version: 6 (50.0)
 * JD-Core Version:    0.6.1-SNAPSHOT
 */