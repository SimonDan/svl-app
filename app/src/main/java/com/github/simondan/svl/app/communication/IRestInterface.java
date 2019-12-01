package com.github.simondan.svl.app.communication;

import android.content.Context;
import com.github.simondan.svl.app.communication.exceptions.*;
import com.github.simondan.svl.communication.auth.UserName;

/**
 * @author Simon Danner, 18.11.2019
 */
public interface IRestInterface
{
  static IRestInterface createForContext(Context pContext)
  {
    return new RestImpl(pContext);
  }

  void registerNewUser(UserName pUserName, String pMail) throws RequestTimeoutException, RequestFailedException;

  void requestAuthRestoreCode(UserName pUserName, String pMail) throws RequestFailedException, RequestTimeoutException;

  void restoreAuthentication(UserName pUserName, String pRestoreCode) throws RequestTimeoutException, RequestFailedException;

  String getDummy() throws AuthenticationImpossibleException, RequestTimeoutException, RequestFailedException;
}
