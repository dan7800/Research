package com.nubee.coinpirates.game;

class GameActivity$1
  implements Runnable
{
  GameActivity$1(GameActivity paramGameActivity)
  {
  }

  public void run()
  {
    GameActivity.setPause(true);
    this.this$0.checkShopCharge(false, 1);
    GameActivity.setPause(false);
  }
}

/* Location:
 * Qualified Name:     com.nubee.coinpirates.game.GameActivity.1
 * Java Class Version: 6 (50.0)
 * JD-Core Version:    0.6.1-SNAPSHOT
 */