package org.apache.james.mime4j.parser;

public abstract interface RecursionMode
{
  public static final int M_FLAT = 3;
  public static final int M_NO_RECURSE = 1;
  public static final int M_RAW = 2;
  public static final int M_RECURSE;
}

/* Location:
 * Qualified Name:     org.apache.james.mime4j.parser.RecursionMode
 * Java Class Version: 6 (50.0)
 * JD-Core Version:    0.6.1-SNAPSHOT
 */