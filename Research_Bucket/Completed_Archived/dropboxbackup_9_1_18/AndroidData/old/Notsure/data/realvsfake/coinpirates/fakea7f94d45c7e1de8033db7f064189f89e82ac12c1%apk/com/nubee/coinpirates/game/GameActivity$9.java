package com.nubee.coinpirates.game;

import com.nubee.coinpirates.common.AnalyticsService;
import com.nubee.coinpirates.payment.PaymentAccessor.Callback;
import com.nubee.coinpirates.payment.PaymentInfoEntity;

class GameActivity$9
  implements PaymentAccessor.Callback
{
  GameActivity$9(GameActivity paramGameActivity, boolean paramBoolean)
  {
  }

  public boolean callback(PaymentInfoEntity paramPaymentInfoEntity)
  {
    if (paramPaymentInfoEntity == null)
    {
      if (this.val$shopFlag)
        this.this$0.dismissProgress();
      GameActivity.setPause(false);
      return false;
    }
    try
    {
      int i = Integer.valueOf(paramPaymentInfoEntity.getQuantity()).intValue();
      int j = Integer.valueOf(paramPaymentInfoEntity.getItemType()).intValue();
      GameActivity.setPause(true);
      if (j == 2)
      {
        GameActivity.wallUp(i * 60);
        GameActivity.access$7(this.this$0).trackEvent("Shop", "Commit", "Wall_" + String.valueOf(i));
      }
      while (true)
      {
        GameActivity.access$6(this.this$0, 103);
        return true;
        GameActivity.charge(i);
        GameActivity.access$7(this.this$0).trackEvent("Shop", "Commit", String.valueOf(i));
      }
    }
    finally
    {
      if (this.val$shopFlag)
        this.this$0.dismissProgress();
      GameActivity.setPause(false);
    }
  }

  public String completeMessage(PaymentInfoEntity paramPaymentInfoEntity)
  {
    if (Integer.valueOf(paramPaymentInfoEntity.getItemType()).intValue() == 2)
    {
      GameActivity localGameActivity2 = this.this$0;
      Object[] arrayOfObject2 = new Object[1];
      arrayOfObject2[0] = Integer.valueOf(paramPaymentInfoEntity.getQuantity());
      return localGameActivity2.getString(2131165272, arrayOfObject2);
    }
    GameActivity localGameActivity1 = this.this$0;
    Object[] arrayOfObject1 = new Object[1];
    arrayOfObject1[0] = Integer.valueOf(paramPaymentInfoEntity.getQuantity());
    return localGameActivity1.getString(2131165269, arrayOfObject1);
  }
}

/* Location:
 * Qualified Name:     com.nubee.coinpirates.game.GameActivity.9
 * Java Class Version: 6 (50.0)
 * JD-Core Version:    0.6.1-SNAPSHOT
 */