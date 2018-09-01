package org.apache.james.mime4j.codec;

import java.util.Iterator;
import java.util.NoSuchElementException;

class UnboundedFifoByteBuffer$1
  implements Iterator<Byte>
{
  private int index = this.this$0.head;
  private int lastReturnedIndex = -1;

  UnboundedFifoByteBuffer$1(UnboundedFifoByteBuffer paramUnboundedFifoByteBuffer)
  {
  }

  public boolean hasNext()
  {
    return this.index != this.this$0.tail;
  }

  public Byte next()
  {
    if (!hasNext())
      throw new NoSuchElementException();
    this.lastReturnedIndex = this.index;
    this.index = UnboundedFifoByteBuffer.access$000(this.this$0, this.index);
    return new Byte(this.this$0.buffer[this.lastReturnedIndex]);
  }

  public void remove()
  {
    if (this.lastReturnedIndex == -1)
      throw new IllegalStateException();
    if (this.lastReturnedIndex == this.this$0.head)
    {
      this.this$0.remove();
      this.lastReturnedIndex = -1;
      return;
    }
    int i = 1 + this.lastReturnedIndex;
    while (i != this.this$0.tail)
      if (i >= this.this$0.buffer.length)
      {
        this.this$0.buffer[(i - 1)] = this.this$0.buffer[0];
        i = 0;
      }
      else
      {
        this.this$0.buffer[(i - 1)] = this.this$0.buffer[i];
        i++;
      }
    this.lastReturnedIndex = -1;
    this.this$0.tail = UnboundedFifoByteBuffer.access$100(this.this$0, this.this$0.tail);
    this.this$0.buffer[this.this$0.tail] = 0;
    this.index = UnboundedFifoByteBuffer.access$100(this.this$0, this.index);
  }
}

/* Location:
 * Qualified Name:     org.apache.james.mime4j.codec.UnboundedFifoByteBuffer.1
 * Java Class Version: 6 (50.0)
 * JD-Core Version:    0.6.1-SNAPSHOT
 */