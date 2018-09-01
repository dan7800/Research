package com.nubee.coinpirates.common;

import android.app.Activity;
import android.content.res.Resources;

public class WorkerThreadResult
  implements Runnable
{
  public static final int ERROR_LEVEL_ATTESTATION = 3;
  public static final int ERROR_LEVEL_AUTH_ERROR = 7;
  public static final int ERROR_LEVEL_BLOCK = 4;
  public static final int ERROR_LEVEL_CAUTION = 0;
  public static final int ERROR_LEVEL_ERROR = 2;
  public static final int ERROR_LEVEL_RETRY_ERROR = 5;
  public static final int ERROR_LEVEL_STAY_ERROR = 6;
  public static final int ERROR_LEVEL_WARNING = 1;
  public static final int RESULT_CANCEL = 3;
  public static final int RESULT_ERROR = 1;
  public static final int RESULT_OK = 0;
  public static final int RESULT_RETRY = 2;
  private Activity activity;
  private String errorInfo;
  private int errorLevel;
  private OnWorkerThreadResultListener listener;
  private OnWorkerMultiThreadResultListener multiListener;
  private int result;
  private String workerParent;

  public WorkerThreadResult(Activity paramActivity, OnWorkerMultiThreadResultListener paramOnWorkerMultiThreadResultListener, String paramString)
  {
    this.activity = paramActivity;
    this.multiListener = paramOnWorkerMultiThreadResultListener;
    this.result = 0;
    this.workerParent = paramString;
  }

  public WorkerThreadResult(Activity paramActivity, OnWorkerThreadResultListener paramOnWorkerThreadResultListener)
  {
    this.activity = paramActivity;
    this.listener = paramOnWorkerThreadResultListener;
    this.result = 0;
  }

  public String getWorkerName()
  {
    return this.workerParent;
  }

  public void run()
  {
    if (this.result != 0)
      switch (this.errorLevel)
      {
      case 4:
      case 5:
      default:
      case 0:
      case 1:
      case 2:
      case 3:
      }
    while (this.listener != null)
    {
      this.listener.onWorkerThreadResult(this.result, this.errorLevel, this.errorInfo);
      return;
      CommonConfig.showDialogForNothing(this.activity, this.errorInfo);
      continue;
      CommonConfig.showDialogForNothing(this.activity, this.activity.getResources().getString(2131165228), this.errorInfo);
      continue;
      CommonConfig.showDialogForNothing(this.activity, this.activity.getResources().getString(2131165227), this.errorInfo);
      continue;
      CommonConfig.showToastMessage(this.activity, this.errorInfo, 0, 30);
    }
    this.multiListener.onWorkerThreadResult(this.result, this.errorLevel, this.errorInfo, this.workerParent);
  }

  public void setErrorInfo(int paramInt, String paramString)
  {
    this.errorLevel = paramInt;
    this.errorInfo = paramString;
    if (paramInt == 5)
    {
      this.result = 2;
      return;
    }
    if (paramInt == 6)
    {
      this.result = 3;
      return;
    }
    this.result = 1;
  }
}

/* Location:
 * Qualified Name:     com.nubee.coinpirates.common.WorkerThreadResult
 * Java Class Version: 6 (50.0)
 * JD-Core Version:    0.6.1-SNAPSHOT
 */