package org.apache.james.mime4j.field.address.parser;

public class ASTgroup_body extends SimpleNode
{
  public ASTgroup_body(int paramInt)
  {
    super(paramInt);
  }

  public ASTgroup_body(AddressListParser paramAddressListParser, int paramInt)
  {
    super(paramAddressListParser, paramInt);
  }

  public Object jjtAccept(AddressListParserVisitor paramAddressListParserVisitor, Object paramObject)
  {
    return paramAddressListParserVisitor.visit(this, paramObject);
  }
}

/* Location:
 * Qualified Name:     org.apache.james.mime4j.field.address.parser.ASTgroup_body
 * Java Class Version: 6 (50.0)
 * JD-Core Version:    0.6.1-SNAPSHOT
 */