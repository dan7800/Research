package org.apache.james.mime4j.message;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.apache.james.mime4j.MimeException;
import org.apache.james.mime4j.MimeIOException;
import org.apache.james.mime4j.parser.AbstractContentHandler;
import org.apache.james.mime4j.parser.Field;
import org.apache.james.mime4j.parser.MimeStreamParser;

public class Header
  implements Iterable<Field>
{
  private Map<String, List<Field>> fieldMap = new HashMap();
  private List<Field> fields = new LinkedList();

  public Header()
  {
  }

  public Header(InputStream paramInputStream)
    throws IOException, MimeIOException
  {
    final MimeStreamParser localMimeStreamParser = new MimeStreamParser();
    localMimeStreamParser.setContentHandler(new AbstractContentHandler()
    {
      public void endHeader()
      {
        localMimeStreamParser.stop();
      }

      public void field(Field paramAnonymousField)
        throws MimeException
      {
        Header.this.addField(paramAnonymousField);
      }
    });
    try
    {
      localMimeStreamParser.parse(paramInputStream);
      return;
    }
    catch (MimeException localMimeException)
    {
      throw new MimeIOException(localMimeException);
    }
  }

  public Header(Header paramHeader)
  {
    Iterator localIterator = paramHeader.fields.iterator();
    while (localIterator.hasNext())
      addField((Field)localIterator.next());
  }

  public void addField(Field paramField)
  {
    Object localObject = (List)this.fieldMap.get(paramField.getName().toLowerCase());
    if (localObject == null)
    {
      localObject = new LinkedList();
      this.fieldMap.put(paramField.getName().toLowerCase(), localObject);
    }
    ((List)localObject).add(paramField);
    this.fields.add(paramField);
  }

  public Field getField(String paramString)
  {
    List localList = (List)this.fieldMap.get(paramString.toLowerCase());
    if ((localList != null) && (!localList.isEmpty()))
      return (Field)localList.get(0);
    return null;
  }

  public List<Field> getFields()
  {
    return Collections.unmodifiableList(this.fields);
  }

  public List<Field> getFields(String paramString)
  {
    String str = paramString.toLowerCase();
    List localList = (List)this.fieldMap.get(str);
    if ((localList == null) || (localList.isEmpty()))
      return Collections.emptyList();
    return Collections.unmodifiableList(localList);
  }

  public Iterator<Field> iterator()
  {
    return Collections.unmodifiableList(this.fields).iterator();
  }

  public int removeFields(String paramString)
  {
    String str = paramString.toLowerCase();
    List localList = (List)this.fieldMap.remove(str);
    if ((localList == null) || (localList.isEmpty()))
      return 0;
    Iterator localIterator = this.fields.iterator();
    while (localIterator.hasNext())
      if (((Field)localIterator.next()).getName().equalsIgnoreCase(paramString))
        localIterator.remove();
    return localList.size();
  }

  public void setField(Field paramField)
  {
    String str = paramField.getName().toLowerCase();
    List localList = (List)this.fieldMap.get(str);
    if ((localList == null) || (localList.isEmpty()))
    {
      addField(paramField);
      return;
    }
    localList.clear();
    localList.add(paramField);
    int i = -1;
    int j = 0;
    Iterator localIterator = this.fields.iterator();
    while (localIterator.hasNext())
    {
      if (((Field)localIterator.next()).getName().equalsIgnoreCase(paramField.getName()))
      {
        localIterator.remove();
        if (i == -1)
          i = j;
      }
      j++;
    }
    this.fields.add(i, paramField);
  }

  public String toString()
  {
    StringBuilder localStringBuilder = new StringBuilder(128);
    Iterator localIterator = this.fields.iterator();
    while (localIterator.hasNext())
    {
      localStringBuilder.append(((Field)localIterator.next()).toString());
      localStringBuilder.append("\r\n");
    }
    return localStringBuilder.toString();
  }
}

/* Location:
 * Qualified Name:     org.apache.james.mime4j.message.Header
 * Java Class Version: 6 (50.0)
 * JD-Core Version:    0.6.1-SNAPSHOT
 */