package org.apache.james.mime4j.message;

public abstract interface Body extends Disposable
{
  public abstract Entity getParent();

  public abstract void setParent(Entity paramEntity);
}

/* Location:
 * Qualified Name:     org.apache.james.mime4j.message.Body
 * Java Class Version: 6 (50.0)
 * JD-Core Version:    0.6.1-SNAPSHOT
 */