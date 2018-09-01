package org.apache.james.mime4j.message;

public class BodyCopier
{
  private BodyCopier()
  {
  }

  public static Body copy(Body paramBody)
  {
    if (paramBody == null)
      throw new IllegalArgumentException("Body is null");
    if ((paramBody instanceof Message))
      return new Message((Message)paramBody);
    if ((paramBody instanceof Multipart))
      return new Multipart((Multipart)paramBody);
    if ((paramBody instanceof SingleBody))
      return ((SingleBody)paramBody).copy();
    throw new IllegalArgumentException("Unsupported body class");
  }
}

/* Location:
 * Qualified Name:     org.apache.james.mime4j.message.BodyCopier
 * Java Class Version: 6 (50.0)
 * JD-Core Version:    0.6.1-SNAPSHOT
 */