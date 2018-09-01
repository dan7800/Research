package com.nubee.coinpirates.common;

public class LabelValueBean
{
  private String label;
  private String number;
  private String value;

  public LabelValueBean()
  {
  }

  public LabelValueBean(String paramString1, String paramString2)
  {
    this.label = paramString1;
    this.value = paramString2;
  }

  public String getLabel()
  {
    return this.label;
  }

  public String getNumber()
  {
    return this.number;
  }

  public String getValue()
  {
    return this.value;
  }

  public void setLabel(String paramString)
  {
    this.label = paramString;
  }

  public void setNumber(String paramString)
  {
    this.number = paramString;
  }

  public void setValue(String paramString)
  {
    this.value = paramString;
  }
}

/* Location:
 * Qualified Name:     com.nubee.coinpirates.common.LabelValueBean
 * Java Class Version: 6 (50.0)
 * JD-Core Version:    0.6.1-SNAPSHOT
 */