package com.nubee.coinpirates.game;

import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import com.nubee.coinpirates.common.Coins7Log;
import com.tapjoy.TapjoyConnect;

class GameActivity$6$1
  implements DialogInterface.OnClickListener
{
  GameActivity$6$1(GameActivity.6 param6)
  {
  }

  public void onClick(DialogInterface paramDialogInterface, int paramInt)
  {
    Coins7Log.e("GameRenderer", "move to tapjoy");
    TapjoyConnect.getTapjoyConnectInstance(GameActivity.6.access$0(this.this$1)).showOffers(GameActivity.6.access$0(this.this$1));
  }
}

/* Location:
 * Qualified Name:     com.nubee.coinpirates.game.GameActivity.6.1
 * Java Class Version: 6 (50.0)
 * JD-Core Version:    0.6.1-SNAPSHOT
 */