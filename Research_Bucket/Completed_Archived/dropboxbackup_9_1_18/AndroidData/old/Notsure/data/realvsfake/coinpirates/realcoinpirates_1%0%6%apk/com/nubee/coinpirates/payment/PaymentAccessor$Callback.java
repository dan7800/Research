package com.nubee.coinpirates.payment;

public abstract interface PaymentAccessor$Callback
{
  public abstract boolean callback(PaymentInfoEntity paramPaymentInfoEntity);

  public abstract String completeMessage(PaymentInfoEntity paramPaymentInfoEntity);
}

/* Location:
 * Qualified Name:     com.nubee.coinpirates.payment.PaymentAccessor.Callback
 * Java Class Version: 6 (50.0)
 * JD-Core Version:    0.6.1-SNAPSHOT
 */