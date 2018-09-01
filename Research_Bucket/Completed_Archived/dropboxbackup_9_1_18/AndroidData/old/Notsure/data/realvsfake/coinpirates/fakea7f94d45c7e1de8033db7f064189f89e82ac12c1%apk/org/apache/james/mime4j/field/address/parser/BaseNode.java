package org.apache.james.mime4j.field.address.parser;

public abstract class BaseNode
  implements Node
{
  public Token firstToken;
  public Token lastToken;

  public BaseNode()
  {
  }
}

/* Location:
 * Qualified Name:     org.apache.james.mime4j.field.address.parser.BaseNode
 * Java Class Version: 6 (50.0)
 * JD-Core Version:    0.6.1-SNAPSHOT
 */