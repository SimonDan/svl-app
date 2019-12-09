package com.github.simondan.svl.app.communication;

import android.content.Context;
import com.github.simondan.svl.app.communication.exceptions.*;
import com.github.simondan.svl.communication.auth.*;

/**
 * @author Simon Danner, 18.11.2019
 */
public interface IRestInterface
{
  static IRestInterface createForContext(Context pContext)
  {
    return new RestImpl(pContext);
  }

  void registerNewUser(IRegistrationRequest pRegistrationRequest) throws RequestTimeoutException, RequestFailedException;

  void requestAuthRestoreCode(IRegistrationRequest pRegistrationData) throws RequestFailedException, RequestTimeoutException;

  void restoreAuthentication(IRestoreAuthRequest pRestoreAuthRequest) throws RequestTimeoutException, RequestFailedException;

  String getDummy() throws AuthenticationImpossibleException, RequestTimeoutException, RequestFailedException;
}
