package com.nubee.coinpirates.game;

import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import com.nubee.coinpirates.common.Coins7Log;
import com.nubee.coinpirates.payment.PaymentAccessor;

class GameActivity$10
  implements DialogInterface.OnClickListener
{
  GameActivity$10(GameActivity paramGameActivity, String paramString1, String paramString2, int paramInt)
  {
  }

  public void onClick(DialogInterface paramDialogInterface, int paramInt)
  {
    PaymentAccessor localPaymentAccessor = new PaymentAccessor(this.this$0, this.val$memberId, this.val$deviceId);
    Coins7Log.e("executePayment", String.valueOf(this.val$type));
    localPaymentAccessor.executePayments(this.val$type);
    GameActivity.setPause(false);
  }
}

/* Location:
 * Qualified Name:     com.nubee.coinpirates.game.GameActivity.10
 * Java Class Version: 6 (50.0)
 * JD-Core Version:    0.6.1-SNAPSHOT
 */