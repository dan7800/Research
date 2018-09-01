package com.nubee.coinpirates.game;

class GameActivity$7
  implements Runnable
{
  GameActivity$7(GameActivity paramGameActivity)
  {
  }

  public void run()
  {
    GameActivity.setPause(true);
    GameActivity.charge(GameActivity.nPendingCoinsToAdd);
    GameActivity.access$6(this.this$0, 104);
    GameActivity localGameActivity1 = this.this$0;
    String str = this.this$0.getString(2131165203);
    GameActivity localGameActivity2 = this.this$0;
    Object[] arrayOfObject = new Object[1];
    arrayOfObject[0] = Integer.valueOf(GameActivity.nPendingCoinsToAdd);
    localGameActivity1.alertDialog(str, localGameActivity2.getString(2131165204, arrayOfObject), null);
    GameActivity.nPendingCoinsToAdd = 0;
    GameActivity.setPause(false);
  }
}

/* Location:
 * Qualified Name:     com.nubee.coinpirates.game.GameActivity.7
 * Java Class Version: 6 (50.0)
 * JD-Core Version:    0.6.1-SNAPSHOT
 */