package com.github.simondan.svl.app.communication;

import android.content.Context;
import com.github.simondan.svl.app.communication.exceptions.*;
import de.adito.ojcms.rest.auth.api.*;

/**
 * @author Simon Danner, 18.11.2019
 */
public interface IRestInterface
{
  static IRestInterface createForContext(Context pContext)
  {
    return new RestImpl(pContext);
  }

  void registerNewUser(RegistrationRequest pRegistrationRequest) throws RequestTimeoutException, RequestFailedException;

  void requestAuthRestoreCode(String pUserMail) throws RequestFailedException, RequestTimeoutException;

  void restoreAuthentication(RestoreAuthenticationRequest pRequest) throws RequestTimeoutException, RequestFailedException;
}
