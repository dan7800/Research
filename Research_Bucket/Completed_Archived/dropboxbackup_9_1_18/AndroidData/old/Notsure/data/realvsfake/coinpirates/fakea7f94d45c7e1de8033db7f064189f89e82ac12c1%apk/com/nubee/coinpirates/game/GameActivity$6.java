package com.nubee.coinpirates.game;

import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import com.nubee.coinpirates.common.Coins7Log;
import com.tapjoy.TapjoyConnect;

class GameActivity$6
  implements Runnable
{
  GameActivity$6(GameActivity paramGameActivity)
  {
  }

  public void run()
  {
    this.this$0.confirmDialog(2131165201, 2131165202, new DialogInterface.OnClickListener()
    {
      public void onClick(DialogInterface paramAnonymousDialogInterface, int paramAnonymousInt)
      {
        Coins7Log.e("GameRenderer", "move to tapjoy");
        TapjoyConnect.getTapjoyConnectInstance(GameActivity.6.this.this$0).showOffers(GameActivity.6.this.this$0);
      }
    }
    , null);
  }
}

/* Location:
 * Qualified Name:     com.nubee.coinpirates.game.GameActivity.6
 * Java Class Version: 6 (50.0)
 * JD-Core Version:    0.6.1-SNAPSHOT
 */