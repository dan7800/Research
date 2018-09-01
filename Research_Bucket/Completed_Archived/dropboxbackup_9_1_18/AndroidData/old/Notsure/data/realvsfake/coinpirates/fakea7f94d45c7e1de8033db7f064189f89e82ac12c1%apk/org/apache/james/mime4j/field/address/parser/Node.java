package org.apache.james.mime4j.field.address.parser;

public abstract interface Node
{
  public abstract Object jjtAccept(AddressListParserVisitor paramAddressListParserVisitor, Object paramObject);

  public abstract void jjtAddChild(Node paramNode, int paramInt);

  public abstract void jjtClose();

  public abstract Node jjtGetChild(int paramInt);

  public abstract int jjtGetNumChildren();

  public abstract Node jjtGetParent();

  public abstract void jjtOpen();

  public abstract void jjtSetParent(Node paramNode);
}

/* Location:
 * Qualified Name:     org.apache.james.mime4j.field.address.parser.Node
 * Java Class Version: 6 (50.0)
 * JD-Core Version:    0.6.1-SNAPSHOT
 */