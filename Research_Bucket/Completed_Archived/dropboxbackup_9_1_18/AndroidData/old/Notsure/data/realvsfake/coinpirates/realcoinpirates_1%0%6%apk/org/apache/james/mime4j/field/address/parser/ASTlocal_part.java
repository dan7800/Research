package org.apache.james.mime4j.field.address.parser;

public class ASTlocal_part extends SimpleNode
{
  public ASTlocal_part(int paramInt)
  {
    super(paramInt);
  }

  public ASTlocal_part(AddressListParser paramAddressListParser, int paramInt)
  {
    super(paramAddressListParser, paramInt);
  }

  public Object jjtAccept(AddressListParserVisitor paramAddressListParserVisitor, Object paramObject)
  {
    return paramAddressListParserVisitor.visit(this, paramObject);
  }
}

/* Location:
 * Qualified Name:     org.apache.james.mime4j.field.address.parser.ASTlocal_part
 * Java Class Version: 6 (50.0)
 * JD-Core Version:    0.6.1-SNAPSHOT
 */