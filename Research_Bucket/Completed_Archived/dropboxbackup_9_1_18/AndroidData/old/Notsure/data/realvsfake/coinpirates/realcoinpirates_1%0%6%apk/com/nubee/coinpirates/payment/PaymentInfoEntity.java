package com.nubee.coinpirates.payment;

public class PaymentInfoEntity
{
  private String item_type;
  private String quantity;

  public PaymentInfoEntity()
  {
  }

  public String getItemType()
  {
    return this.item_type;
  }

  public String getQuantity()
  {
    return this.quantity;
  }

  public void setItemType(String paramString)
  {
    this.item_type = paramString;
  }

  public void setQuantity(String paramString)
  {
    this.quantity = paramString;
  }
}

/* Location:
 * Qualified Name:     com.nubee.coinpirates.payment.PaymentInfoEntity
 * Java Class Version: 6 (50.0)
 * JD-Core Version:    0.6.1-SNAPSHOT
 */