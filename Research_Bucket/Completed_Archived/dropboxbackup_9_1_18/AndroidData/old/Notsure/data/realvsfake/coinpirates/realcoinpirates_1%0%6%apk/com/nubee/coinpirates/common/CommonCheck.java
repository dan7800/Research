package com.nubee.coinpirates.common;

import android.content.Context;
import android.content.res.Resources;
import java.io.UnsupportedEncodingException;

public class CommonCheck
{
  private static final String CHECK_MAIL_FORM = "[\\w\\.\\-]+@(?:[\\w\\-]+\\.)+[\\w\\-]+";
  private static final String CHECK_PASSWORD_FORM = "^[a-zA-Z0-9]+$";

  public CommonCheck()
  {
  }

  public static boolean checkMailAddress(String paramString, Context paramContext)
  {
    boolean bool = true;
    try
    {
      int i = paramString.getBytes("UTF-8").length;
      if (CommonConfig.isBlank(paramString))
      {
        bool = false;
        CommonConfig.showToastMessage(paramContext, paramContext.getResources().getString(2131165249), 0);
        return false;
      }
      if (i >= 255)
      {
        bool = false;
        CommonConfig.showToastMessage(paramContext, paramContext.getResources().getString(2131165250), 0);
        return false;
      }
    }
    catch (UnsupportedEncodingException localUnsupportedEncodingException)
    {
      Coins7Log.e("AndroidNews:changeMailAddress", localUnsupportedEncodingException.getMessage(), localUnsupportedEncodingException);
      return bool;
    }
    if (!paramString.matches("[\\w\\.\\-]+@(?:[\\w\\-]+\\.)+[\\w\\-]+"))
    {
      bool = false;
      CommonConfig.showToastMessage(paramContext, paramContext.getResources().getString(2131165251), 0);
    }
    return bool;
  }

  public static boolean checkNubeeId(String paramString, Context paramContext)
  {
    if (CommonConfig.isBlank(paramString))
    {
      CommonConfig.showToastMessage(paramContext, paramContext.getResources().getString(2131165235), 0, 80);
      return false;
    }
    int i = paramString.length();
    if ((i < 4) || (i > 12))
    {
      CommonConfig.showToastMessage(paramContext, paramContext.getResources().getString(2131165236), 0, 80);
      return false;
    }
    if (!paramString.matches("^[a-zA-Z0-9]+$"))
    {
      CommonConfig.showToastMessage(paramContext, paramContext.getResources().getString(2131165237), 0, 80);
      return false;
    }
    return true;
  }

  public static boolean checkPassword(String paramString, Context paramContext)
  {
    boolean bool = true;
    int i;
    try
    {
      i = paramString.getBytes("UTF-8").length;
      if (CommonConfig.isBlank(paramString))
      {
        bool = false;
        CommonConfig.showToastMessage(paramContext, paramContext.getResources().getString(2131165252), 0);
        return false;
        bool = false;
        CommonConfig.showToastMessage(paramContext, paramContext.getResources().getString(2131165253), 0);
        return false;
      }
    }
    catch (UnsupportedEncodingException localUnsupportedEncodingException)
    {
      Coins7Log.e("AndroidNews:changePassword", localUnsupportedEncodingException.getMessage(), localUnsupportedEncodingException);
      return bool;
    }
    while (true)
    {
      if (!paramString.matches("^[a-zA-Z0-9]+$"))
      {
        bool = false;
        CommonConfig.showToastMessage(paramContext, paramContext.getResources().getString(2131165254), 0);
      }
      return bool;
      if (i >= 6)
        if (i <= 32);
    }
  }
}

/* Location:
 * Qualified Name:     com.nubee.coinpirates.common.CommonCheck
 * Java Class Version: 6 (50.0)
 * JD-Core Version:    0.6.1-SNAPSHOT
 */