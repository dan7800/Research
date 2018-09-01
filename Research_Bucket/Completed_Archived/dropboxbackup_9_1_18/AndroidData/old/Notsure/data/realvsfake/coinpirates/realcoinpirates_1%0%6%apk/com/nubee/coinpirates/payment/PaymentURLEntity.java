package com.nubee.coinpirates.payment;

public class PaymentURLEntity
{
  private String paymentKey;
  private String paymentUrl;

  public PaymentURLEntity()
  {
  }

  public String getPaymentKey()
  {
    return this.paymentKey;
  }

  public String getPaymentUrl()
  {
    return this.paymentUrl;
  }

  public void setPaymentKey(String paramString)
  {
    this.paymentKey = paramString;
  }

  public void setPaymentUrl(String paramString)
  {
    this.paymentUrl = paramString;
  }
}

/* Location:
 * Qualified Name:     com.nubee.coinpirates.payment.PaymentURLEntity
 * Java Class Version: 6 (50.0)
 * JD-Core Version:    0.6.1-SNAPSHOT
 */