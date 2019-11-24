package com.github.simondan.svl.app.communication;

import android.content.Context;
import com.github.simondan.svl.app.communication.exceptions.*;

/**
 * @author Simon Danner, 18.11.2019
 */
public interface IRestInterface
{
  static IRestInterface createForContext(Context pContext)
  {
    return new RestImpl(pContext);
  }

  void registerNewUser(String pFirstName, String pLastName, String pMail) throws RequestTimeoutException, RequestFailedException;

  void requestAuthRestoreCode(String pFirstName, String pLastName, String pMail) throws RequestFailedException,
      RequestTimeoutException;

  void restoreAuthentication(String pFirstName, String pLastName, String pRestoreCode) throws RequestTimeoutException,
      RequestFailedException;

  String getDummy() throws AuthenticationImpossibleException, RequestTimeoutException, RequestFailedException;
}
