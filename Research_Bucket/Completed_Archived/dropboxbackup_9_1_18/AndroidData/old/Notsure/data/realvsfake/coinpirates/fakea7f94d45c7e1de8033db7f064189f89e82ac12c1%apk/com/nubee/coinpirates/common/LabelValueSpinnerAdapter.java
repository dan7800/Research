package com.nubee.coinpirates.common;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import java.util.ArrayList;

public class LabelValueSpinnerAdapter extends ArrayAdapter<LabelValueBean>
{
  private LayoutInflater inflater = null;
  private ArrayList<LabelValueBean> items = null;

  public LabelValueSpinnerAdapter(Context paramContext, int paramInt, ArrayList<LabelValueBean> paramArrayList)
  {
    super(paramContext, paramInt, paramArrayList);
    this.items = paramArrayList;
    this.inflater = ((LayoutInflater)paramContext.getSystemService("layout_inflater"));
  }

  public void add(String paramString1, String paramString2)
  {
    super.add(new LabelValueBean(paramString1, paramString2));
  }

  public View getDropDownView(int paramInt, View paramView, ViewGroup paramViewGroup)
  {
    View localView = paramView;
    if (paramView == null)
      localView = this.inflater.inflate(17367049, null);
    LabelValueBean localLabelValueBean = (LabelValueBean)this.items.get(paramInt);
    ((TextView)localView.findViewById(16908308)).setText(localLabelValueBean.getLabel());
    return localView;
  }

  public int getPositionByValue(String paramString)
  {
    for (int i = 0; ; i++)
    {
      if (i >= this.items.size())
        return -1;
      if (paramString == null)
      {
        if (paramString == ((LabelValueBean)this.items.get(i)).getValue())
          return i;
      }
      else if (paramString.equals(((LabelValueBean)this.items.get(i)).getValue()))
        return i;
    }
  }

  public View getView(int paramInt, View paramView, ViewGroup paramViewGroup)
  {
    View localView = paramView;
    if (paramView == null)
      localView = this.inflater.inflate(17367048, null);
    LabelValueBean localLabelValueBean = (LabelValueBean)this.items.get(paramInt);
    ((TextView)localView.findViewById(16908308)).setText(localLabelValueBean.getLabel());
    return localView;
  }
}

/* Location:
 * Qualified Name:     com.nubee.coinpirates.common.LabelValueSpinnerAdapter
 * Java Class Version: 6 (50.0)
 * JD-Core Version:    0.6.1-SNAPSHOT
 */