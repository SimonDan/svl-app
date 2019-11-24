package com.github.simondan.svl.app.communication.exceptions;

import okhttp3.Response;

/**
 * @author Simon Danner, 09.11.2019
 */
public class InternalCommunicationException extends RuntimeException
{
  public InternalCommunicationException(Throwable pCause)
  {
    super(pCause);
  }

  public InternalCommunicationException(String pMessage)
  {
    super(pMessage);
  }

  public InternalCommunicationException(Response pResponse)
  {
    this("Internal server error: " + pResponse.message());
  }
}
