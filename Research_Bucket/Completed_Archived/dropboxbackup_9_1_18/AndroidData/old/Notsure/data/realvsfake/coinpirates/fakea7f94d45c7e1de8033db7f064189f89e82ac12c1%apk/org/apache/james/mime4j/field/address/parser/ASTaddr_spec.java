package org.apache.james.mime4j.field.address.parser;

public class ASTaddr_spec extends SimpleNode
{
  public ASTaddr_spec(int paramInt)
  {
    super(paramInt);
  }

  public ASTaddr_spec(AddressListParser paramAddressListParser, int paramInt)
  {
    super(paramAddressListParser, paramInt);
  }

  public Object jjtAccept(AddressListParserVisitor paramAddressListParserVisitor, Object paramObject)
  {
    return paramAddressListParserVisitor.visit(this, paramObject);
  }
}

/* Location:
 * Qualified Name:     org.apache.james.mime4j.field.address.parser.ASTaddr_spec
 * Java Class Version: 6 (50.0)
 * JD-Core Version:    0.6.1-SNAPSHOT
 */