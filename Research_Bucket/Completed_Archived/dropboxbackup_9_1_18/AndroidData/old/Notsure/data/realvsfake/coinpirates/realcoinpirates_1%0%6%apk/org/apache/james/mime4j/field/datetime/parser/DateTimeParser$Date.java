package org.apache.james.mime4j.field.datetime.parser;

class DateTimeParser$Date
{
  private int day;
  private int month;
  private String year;

  public DateTimeParser$Date(String paramString, int paramInt1, int paramInt2)
  {
    this.year = paramString;
    this.month = paramInt1;
    this.day = paramInt2;
  }

  public int getDay()
  {
    return this.day;
  }

  public int getMonth()
  {
    return this.month;
  }

  public String getYear()
  {
    return this.year;
  }
}

/* Location:
 * Qualified Name:     org.apache.james.mime4j.field.datetime.parser.DateTimeParser.Date
 * Java Class Version: 6 (50.0)
 * JD-Core Version:    0.6.1-SNAPSHOT
 */