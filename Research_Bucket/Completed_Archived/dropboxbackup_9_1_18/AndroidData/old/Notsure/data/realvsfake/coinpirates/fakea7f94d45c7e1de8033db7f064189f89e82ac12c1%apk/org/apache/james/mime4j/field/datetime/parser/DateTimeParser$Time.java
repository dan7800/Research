package org.apache.james.mime4j.field.datetime.parser;

class DateTimeParser$Time
{
  private int hour;
  private int minute;
  private int second;
  private int zone;

  public DateTimeParser$Time(int paramInt1, int paramInt2, int paramInt3, int paramInt4)
  {
    this.hour = paramInt1;
    this.minute = paramInt2;
    this.second = paramInt3;
    this.zone = paramInt4;
  }

  public int getHour()
  {
    return this.hour;
  }

  public int getMinute()
  {
    return this.minute;
  }

  public int getSecond()
  {
    return this.second;
  }

  public int getZone()
  {
    return this.zone;
  }
}

/* Location:
 * Qualified Name:     org.apache.james.mime4j.field.datetime.parser.DateTimeParser.Time
 * Java Class Version: 6 (50.0)
 * JD-Core Version:    0.6.1-SNAPSHOT
 */