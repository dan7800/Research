package org.apache.james.mime4j.message;

import java.io.IOException;
import java.io.OutputStream;

public abstract class SingleBody
  implements Body
{
  private Entity parent = null;

  protected SingleBody()
  {
  }

  public SingleBody copy()
  {
    throw new UnsupportedOperationException();
  }

  public void dispose()
  {
  }

  public Entity getParent()
  {
    return this.parent;
  }

  public void setParent(Entity paramEntity)
  {
    this.parent = paramEntity;
  }

  public abstract void writeTo(OutputStream paramOutputStream)
    throws IOException;
}

/* Location:
 * Qualified Name:     org.apache.james.mime4j.message.SingleBody
 * Java Class Version: 6 (50.0)
 * JD-Core Version:    0.6.1-SNAPSHOT
 */