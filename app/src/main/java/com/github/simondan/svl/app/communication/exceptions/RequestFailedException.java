package com.github.simondan.svl.app.communication.exceptions;

import com.github.simondan.svl.communication.ESupportedHttpStatus;
import okhttp3.Response;

/**
 * @author Simon Danner, 09.11.2019
 */
public class RequestFailedException extends Exception
{
  private final ESupportedHttpStatus status;
  private final String serverMessage;

  public RequestFailedException(Response pResponse)
  {
    super("Webservice request for URL " + pResponse.request().url() + " was not successful! status: " + pResponse.code() +
              ", message: " + pResponse.message());
    status = ESupportedHttpStatus.byCode(pResponse.code());
    serverMessage = pResponse.message();
  }

  public ESupportedHttpStatus getStatus()
  {
    return status;
  }

  public String getServerMessage()
  {
    return serverMessage;
  }
}
