package com.google.android.apps.analytics;

abstract interface PipelinedRequester$Callbacks
{
  public abstract void pipelineModeChanged(boolean paramBoolean);

  public abstract void requestSent();

  public abstract void serverError(int paramInt);
}

/* Location:
 * Qualified Name:     com.google.android.apps.analytics.PipelinedRequester.Callbacks
 * Java Class Version: 6 (50.0)
 * JD-Core Version:    0.6.1-SNAPSHOT
 */