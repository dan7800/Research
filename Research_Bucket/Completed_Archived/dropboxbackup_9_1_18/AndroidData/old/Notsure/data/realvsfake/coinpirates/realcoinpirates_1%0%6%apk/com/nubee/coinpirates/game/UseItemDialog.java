package com.nubee.coinpirates.game;

import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.Context;
import android.content.res.Resources;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

public class UseItemDialog extends Dialog
  implements AdapterView.OnItemSelectedListener, View.OnClickListener
{
  public static final int BLUE_MEDAL = 2;
  public static final int GOLD_MEDAL = 0;
  public static final int ITEM_TYPE_BOOK = 4;
  public static final int ITEM_TYPE_COMPASS = 0;
  public static final int ITEM_TYPE_DICE = 3;
  public static final int ITEM_TYPE_GEM = 2;
  public static final int ITEM_TYPE_MIRROR = 1;
  public static final int ITEM_TYPE_MUSIC_BOX = 5;
  public static final int NUM_OF_ITEM_TYPE = 6;
  static final int NUM_USE_ITEM_COIN = 9;
  public static final int RED_BARREL = 12;
  public static final int RED_MEDAL = 1;
  public static final int TREASURE_BOX = 28;
  static final int[] USE_ITEM_COIN = { 5, 12, 21, 32, 45, 60, 77, 99, 120 };
  static sUseItemExtra[] USE_ITEM_EXTRA = new sUseItemExtra[6];
  public static final int USE_ITEM_EXTRA_777 = -2;
  public static final int USE_ITEM_EXTRA_GOLD_MEDAL = -1;

  static
  {
    USE_ITEM_EXTRA[0] = new sUseItemExtra(-1, 120);
    USE_ITEM_EXTRA[1] = new sUseItemExtra(2, 30);
    USE_ITEM_EXTRA[2] = new sUseItemExtra(12, 3);
    USE_ITEM_EXTRA[3] = new sUseItemExtra(28, 3);
    USE_ITEM_EXTRA[4] = new sUseItemExtra(1, 15);
    USE_ITEM_EXTRA[5] = new sUseItemExtra(-2, 0);
  }

  public UseItemDialog(Context paramContext)
  {
    super(paramContext);
  }

  public UseItemDialog(Context paramContext, int paramInt)
  {
    super(paramContext, paramInt);
  }

  public static UseItemDialog show(Context paramContext)
  {
    int i = GameActivity.getUseItemType();
    int j = GameActivity.getUseItemSet();
    if (j > 9)
      j = 9;
    if (j <= 0)
      return null;
    View localView = ((LayoutInflater)paramContext.getSystemService("layout_inflater")).inflate(2130903049, null);
    String str;
    int k;
    Spinner localSpinner;
    ArrayAdapter localArrayAdapter;
    switch (i)
    {
    default:
      str = "COMPASSES";
      k = 2130837523;
      ((ImageView)localView.findViewById(2131296312)).setImageResource(k);
      ((TextView)localView.findViewById(2131296313)).setText(str);
      ((TextView)localView.findViewById(2131296315)).setVisibility(4);
      localSpinner = (Spinner)localView.findViewById(2131296314);
      localArrayAdapter = new ArrayAdapter(paramContext, 17367048);
      localArrayAdapter.setDropDownViewResource(17367049);
    case 1:
    case 2:
    case 3:
    case 4:
    case 5:
    }
    for (int m = 0; ; m++)
    {
      if (m >= j)
      {
        localSpinner.setAdapter(localArrayAdapter);
        UseItemDialog localUseItemDialog = new UseItemDialog(paramContext);
        localUseItemDialog.requestWindowFeature(1);
        localUseItemDialog.setContentView(localView);
        localSpinner.setOnItemSelectedListener(localUseItemDialog);
        localView.findViewById(2131296318).setOnClickListener(localUseItemDialog);
        localView.findViewById(2131296319).setOnClickListener(localUseItemDialog);
        localUseItemDialog.show();
        return localUseItemDialog;
        str = "MIRRORS";
        k = 2130837526;
        break;
        str = "JEWELS";
        k = 2130837525;
        break;
        str = "DICE";
        k = 2130837524;
        break;
        str = "BOOKS";
        k = 2130837520;
        break;
        str = "MUSICAL BOX";
        k = 2130837527;
        break;
      }
      localArrayAdapter.add(String.valueOf(m + 1) + "SET");
    }
  }

  public void onClick(View paramView)
  {
    if (paramView.getId() == 2131296318)
    {
      int i = ((Spinner)findViewById(2131296314)).getSelectedItemPosition();
      Log.e("onClick", i);
      if (GameActivity.useItem(GameActivity.getUseItemType(), i + 1))
        new AlertDialog.Builder(getContext()).setMessage(getContext().getResources().getString(2131165275)).setPositiveButton(2131165186, null).show();
      hide();
      return;
    }
    cancel();
  }

  public void onItemSelected(AdapterView<?> paramAdapterView, View paramView, int paramInt, long paramLong)
  {
    Log.e("onItemSelected", paramInt + "," + paramLong);
    int j;
    int k;
    if (paramInt < 8)
    {
      j = 2130837522;
      k = USE_ITEM_COIN[paramInt];
      ((TextView)findViewById(2131296315)).setVisibility(4);
    }
    TextView localTextView;
    while (true)
    {
      ((ImageView)findViewById(2131296316)).setImageResource(j);
      localTextView = (TextView)findViewById(2131296317);
      if (k <= 0)
        break;
      localTextView.setText("×" + k);
      return;
      int i = GameActivity.getUseItemType();
      sUseItemExtra localsUseItemExtra = USE_ITEM_EXTRA[i];
      switch (localsUseItemExtra.type)
      {
      default:
        j = 2130837522;
      case 1:
      case 2:
      case 12:
      case 28:
      case -2:
      }
      while (true)
      {
        k = localsUseItemExtra.count;
        if (localsUseItemExtra.type < 0)
          break label260;
        ((TextView)findViewById(2131296315)).setVisibility(0);
        break;
        j = 2130837528;
        continue;
        j = 2130837519;
        continue;
        j = 2130837517;
        continue;
        j = 2130837521;
        continue;
        j = 2130837515;
      }
      label260: ((TextView)findViewById(2131296315)).setVisibility(4);
    }
    localTextView.setText("");
  }

  public void onNothingSelected(AdapterView<?> paramAdapterView)
  {
  }

  static class sUseItemExtra
  {
    int count;
    int type;

    public sUseItemExtra(int paramInt1, int paramInt2)
    {
      this.type = paramInt1;
      this.count = paramInt2;
    }
  }
}

/* Location:
 * Qualified Name:     com.nubee.coinpirates.game.UseItemDialog
 * Java Class Version: 6 (50.0)
 * JD-Core Version:    0.6.1-SNAPSHOT
 */