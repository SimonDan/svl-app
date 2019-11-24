package com.github.simondan.svl.app.communication.exceptions;

import com.github.simondan.svl.app.communication.config.TimeoutConfig;

/**
 * @author Simon Danner, 09.11.2019
 */
public class RequestTimeoutException extends Exception
{
  public RequestTimeoutException(TimeoutConfig pTimeoutConfig, Throwable pCause)
  {
    super("The server could not be reached in " + pTimeoutConfig.getTimeout() + " " + pTimeoutConfig.getTimeUnit() + "!", pCause);
  }
}
