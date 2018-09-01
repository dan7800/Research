package org.apache.james.mime4j.field.address;

import java.util.Iterator;
import org.apache.james.mime4j.field.address.parser.Node;
import org.apache.james.mime4j.field.address.parser.SimpleNode;

class Builder$ChildNodeIterator
  implements Iterator<Node>
{
  private int index;
  private int len;
  private SimpleNode simpleNode;

  public Builder$ChildNodeIterator(SimpleNode paramSimpleNode)
  {
    this.simpleNode = paramSimpleNode;
    this.len = paramSimpleNode.jjtGetNumChildren();
    this.index = 0;
  }

  public boolean hasNext()
  {
    return this.index < this.len;
  }

  public Node next()
  {
    SimpleNode localSimpleNode = this.simpleNode;
    int i = this.index;
    this.index = (i + 1);
    return localSimpleNode.jjtGetChild(i);
  }

  public void remove()
  {
    throw new UnsupportedOperationException();
  }
}

/* Location:
 * Qualified Name:     org.apache.james.mime4j.field.address.Builder.ChildNodeIterator
 * Java Class Version: 6 (50.0)
 * JD-Core Version:    0.6.1-SNAPSHOT
 */