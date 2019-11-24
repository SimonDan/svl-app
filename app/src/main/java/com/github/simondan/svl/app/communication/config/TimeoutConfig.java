package com.github.simondan.svl.app.communication.config;

import java.util.concurrent.TimeUnit;

/**
 * @author Simon Danner, 09.11.2019
 */
public final class TimeoutConfig
{
  private final TimeUnit timeUnit;
  private final long value;

  public TimeoutConfig(TimeUnit pTimeUnit, long pValue)
  {
    timeUnit = pTimeUnit;
    value = pValue;
  }

  public TimeUnit getTimeUnit()
  {
    return timeUnit;
  }

  public long getTimeout()
  {
    return value;
  }
}
