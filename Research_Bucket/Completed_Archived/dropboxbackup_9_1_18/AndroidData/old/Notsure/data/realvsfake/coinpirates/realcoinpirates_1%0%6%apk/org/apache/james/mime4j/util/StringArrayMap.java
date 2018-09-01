package org.apache.james.mime4j.util;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.Set;

public class StringArrayMap
  implements Serializable
{
  private static final long serialVersionUID = -5833051164281786907L;
  private final Map<String, Object> map = new HashMap();

  public StringArrayMap()
  {
  }

  public static Map<String, String[]> asMap(Map<String, Object> paramMap)
  {
    HashMap localHashMap = new HashMap(paramMap.size());
    Iterator localIterator = paramMap.entrySet().iterator();
    while (localIterator.hasNext())
    {
      Map.Entry localEntry = (Map.Entry)localIterator.next();
      String[] arrayOfString = asStringArray(localEntry.getValue());
      localHashMap.put(localEntry.getKey(), arrayOfString);
    }
    return Collections.unmodifiableMap(localHashMap);
  }

  public static String asString(Object paramObject)
  {
    if (paramObject == null)
      return null;
    if ((paramObject instanceof String))
      return (String)paramObject;
    if ((paramObject instanceof String[]))
      return ((String[])(String[])paramObject)[0];
    if ((paramObject instanceof List))
      return (String)((List)paramObject).get(0);
    throw new IllegalStateException("Invalid parameter class: " + paramObject.getClass().getName());
  }

  public static String[] asStringArray(Object paramObject)
  {
    if (paramObject == null)
      return null;
    if ((paramObject instanceof String))
    {
      String[] arrayOfString = new String[1];
      arrayOfString[0] = ((String)paramObject);
      return arrayOfString;
    }
    if ((paramObject instanceof String[]))
      return (String[])paramObject;
    if ((paramObject instanceof List))
    {
      List localList = (List)paramObject;
      return (String[])localList.toArray(new String[localList.size()]);
    }
    throw new IllegalStateException("Invalid parameter class: " + paramObject.getClass().getName());
  }

  public static Enumeration<String> asStringEnum(Object paramObject)
  {
    if (paramObject == null)
      return null;
    if ((paramObject instanceof String))
      return new Enumeration()
      {
        private Object value = this.val$pValue;

        public boolean hasMoreElements()
        {
          return this.value != null;
        }

        public String nextElement()
        {
          if (this.value == null)
            throw new NoSuchElementException();
          String str = (String)this.value;
          this.value = null;
          return str;
        }
      };
    if ((paramObject instanceof String[]))
      return new Enumeration()
      {
        private int offset;

        public boolean hasMoreElements()
        {
          return this.offset < this.val$values.length;
        }

        public String nextElement()
        {
          if (this.offset >= this.val$values.length)
            throw new NoSuchElementException();
          String[] arrayOfString = this.val$values;
          int i = this.offset;
          this.offset = (i + 1);
          return arrayOfString[i];
        }
      };
    if ((paramObject instanceof List))
      return Collections.enumeration((List)paramObject);
    throw new IllegalStateException("Invalid parameter class: " + paramObject.getClass().getName());
  }

  protected void addMapValue(Map<String, Object> paramMap, String paramString1, String paramString2)
  {
    Object localObject = paramMap.get(paramString1);
    if (localObject == null)
      localObject = paramString2;
    while (true)
    {
      paramMap.put(paramString1, localObject);
      return;
      if ((localObject instanceof String))
      {
        ArrayList localArrayList1 = new ArrayList();
        localArrayList1.add(localObject);
        localArrayList1.add(paramString2);
        localObject = localArrayList1;
      }
      else if ((localObject instanceof List))
      {
        ((List)localObject).add(paramString2);
      }
      else
      {
        if (!(localObject instanceof String[]))
          break;
        ArrayList localArrayList2 = new ArrayList();
        String[] arrayOfString = (String[])localObject;
        int i = arrayOfString.length;
        for (int j = 0; j < i; j++)
          localArrayList2.add(arrayOfString[j]);
        localArrayList2.add(paramString2);
        localObject = localArrayList2;
      }
    }
    throw new IllegalStateException("Invalid object type: " + localObject.getClass().getName());
  }

  public void addValue(String paramString1, String paramString2)
  {
    addMapValue(this.map, convertName(paramString1), paramString2);
  }

  protected String convertName(String paramString)
  {
    return paramString.toLowerCase();
  }

  public Map<String, String[]> getMap()
  {
    return asMap(this.map);
  }

  public String[] getNameArray()
  {
    Set localSet = this.map.keySet();
    return (String[])localSet.toArray(new String[localSet.size()]);
  }

  public Enumeration<String> getNames()
  {
    return Collections.enumeration(this.map.keySet());
  }

  public String getValue(String paramString)
  {
    return asString(this.map.get(convertName(paramString)));
  }

  public Enumeration<String> getValueEnum(String paramString)
  {
    return asStringEnum(this.map.get(convertName(paramString)));
  }

  public String[] getValues(String paramString)
  {
    return asStringArray(this.map.get(convertName(paramString)));
  }
}

/* Location:
 * Qualified Name:     org.apache.james.mime4j.util.StringArrayMap
 * Java Class Version: 6 (50.0)
 * JD-Core Version:    0.6.1-SNAPSHOT
 */