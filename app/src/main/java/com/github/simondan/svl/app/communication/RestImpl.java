package com.github.simondan.svl.app.communication;

import android.content.Context;
import com.github.simondan.svl.app.communication.exceptions.*;
import com.github.simondan.svl.communication.auth.SVLAuthenticationResponse;
import de.adito.ojcms.rest.auth.api.*;

import java.util.Objects;
import java.util.function.Function;

import static de.adito.ojcms.rest.auth.api.RestoreAuthenticationRequest.USER_MAIL;

/**
 * @author Simon Danner, 18.11.2019
 */
public class RestImpl implements IRestInterface
{
  private static final String PATH_AUTH = "authentication";

  private final Context context;
  private final ICredentialsStore credentialsStore;

  RestImpl(Context pContext)
  {
    context = Objects.requireNonNull(pContext, "The context must not be null!");
    credentialsStore = ICredentialsStore.createForContext(pContext);
  }

  @Override
  public void registerNewUser(RegistrationRequest pRegistrationRequest) throws RequestFailedException,
      RequestTimeoutException
  {
    _newAuthCall("register", pRegistrationRequest, RegistrationRequest.class, pRequest ->
        pRequest.getValue(RegistrationRequest.USER_MAIL));
  }

  @Override
  public void requestAuthRestoreCode(String pUserMail) throws RequestFailedException,
      RequestTimeoutException
  {
    RestBuilder.buildNoResultCall(context)
        .path(PATH_AUTH + "/requestCode")
        .method(EMethod.PUT)
        .textParam(pUserMail)
        .executeCallNoAuthentication();
  }

  @Override
  public void restoreAuthentication(RestoreAuthenticationRequest pRestoreAuthRequest) throws RequestFailedException,
      RequestTimeoutException
  {
    _newAuthCall("restore", pRestoreAuthRequest, RestoreAuthenticationRequest.class,
        pRequest -> pRequest.getValue(USER_MAIL));
  }

  private <REQUEST> void _newAuthCall(String pAuthPath, REQUEST pRequest, Class<? super REQUEST> pRequestType, Function<REQUEST,
      String> pUserMailRetriever) throws RequestTimeoutException, RequestFailedException
  {
    final SVLAuthenticationResponse authenticationResponse = RestBuilder.buildCall(context, SVLAuthenticationResponse.class)
        .path(PATH_AUTH + "/" + pAuthPath)
        .method(EMethod.POST)
        .jsonParam(pRequest, pRequestType)
        .executeCallNoAuthentication();

    credentialsStore.setUserMail(pUserMailRetriever.apply(pRequest));
    credentialsStore.saveNewAuthData(authenticationResponse);
  }
}
