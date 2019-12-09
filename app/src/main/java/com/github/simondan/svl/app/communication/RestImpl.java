package com.github.simondan.svl.app.communication;

import android.content.Context;
import com.github.simondan.svl.app.communication.exceptions.*;
import com.github.simondan.svl.communication.auth.*;

import java.util.Objects;
import java.util.function.Function;

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
  public void registerNewUser(IRegistrationRequest pRegistrationRequest) throws RequestFailedException,
      RequestTimeoutException
  {
    _newAuthCall("register", pRegistrationRequest, IRegistrationRequest.class, IRegistrationRequest::getUserName);
  }

  @Override
  public void requestAuthRestoreCode(IRegistrationRequest pRegistrationData) throws RequestFailedException,
      RequestTimeoutException
  {
    RestBuilder.buildNoResultCall(context)
        .path(PATH_AUTH + "/requestCode")
        .method(EMethod.PUT)
        .jsonParam(pRegistrationData, IRegistrationRequest.class)
        .executeCallNoAuthentication();
  }

  @Override
  public void restoreAuthentication(IRestoreAuthRequest pRestoreAuthRequest) throws RequestFailedException,
      RequestTimeoutException
  {
    _newAuthCall("restore", pRestoreAuthRequest, IRestoreAuthRequest.class, IRestoreAuthRequest::getUserName);
  }

  @Override
  public String getDummy() throws AuthenticationImpossibleException, RequestTimeoutException, RequestFailedException
  {
    return RestBuilder.buildCall(context, String.class)
        .path("dummy/test")
        .method(EMethod.GET)
        .executeCall();
  }

  private <REQUEST> void _newAuthCall(String pAuthPath, REQUEST pRequest, Class<? super REQUEST> pRequestType, Function<REQUEST,
      UserName> pUserNameRetriever) throws RequestTimeoutException, RequestFailedException
  {
    final AuthenticationResponse authenticationResponse = RestBuilder.buildCall(context, AuthenticationResponse.class)
        .path(PATH_AUTH + "/" + pAuthPath)
        .method(EMethod.POST)
        .jsonParam(pRequest, pRequestType)
        .executeCallNoAuthentication();

    credentialsStore.setUserName(pUserNameRetriever.apply(pRequest));
    credentialsStore.saveNewAuthData(authenticationResponse);
  }
}
