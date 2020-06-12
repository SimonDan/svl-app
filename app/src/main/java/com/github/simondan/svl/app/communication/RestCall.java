package com.github.simondan.svl.app.communication;

import com.github.simondan.svl.app.communication.config.TimeoutConfig;
import com.github.simondan.svl.app.communication.exceptions.*;
import com.github.simondan.svl.communication.ESupportedHttpStatus;
import com.github.simondan.svl.communication.auth.SVLAuthenticationResponse;
import okhttp3.*;

import java.io.IOException;

import static de.adito.ojcms.rest.auth.util.OJGsonSerializer.GSON_INSTANCE;

/**
 * @author Simon Danner, 09.11.2019
 */
class RestCall<RESULT>
{
  private final TimeoutConfig timeoutConfig;
  private final ICredentialsStore credentialsStore;
  private final IRestCallConfig<RESULT> callConfig;

  RestCall(TimeoutConfig pTimeoutConfig, ICredentialsStore pCredentialsStore, IRestCallConfig<RESULT> pCallConfig)
  {
    timeoutConfig = pTimeoutConfig;
    credentialsStore = pCredentialsStore;
    callConfig = pCallConfig;
  }

  RESULT callRestInterface() throws AuthenticationImpossibleException, RequestTimeoutException, RequestFailedException
  {
    return _callAndHandleAuthentication();
  }

  RESULT callRestInterfaceNoAuth() throws RequestTimeoutException, RequestFailedException
  {
    return _callByConfigNoAuth();
  }

  private RESULT _callAndHandleAuthentication() throws RequestTimeoutException, AuthenticationImpossibleException, RequestFailedException
  {
    if (!credentialsStore.areCredentialsInitialized())
      throw new InternalCommunicationException("Unable to perform rest call with authentication! Credential store is not initialized!");

    try
    {
      return _callByConfigWithAuth();
    }
    catch (AuthenticationImpossibleException pE)
    {
      return _callButRequestNewAuthBefore();
    }
  }

  private RESULT _callButRequestNewAuthBefore() throws AuthenticationImpossibleException, RequestTimeoutException, RequestFailedException
  {
    //Request new authentication credentials from server
    final SVLAuthenticationResponse newAuthentication = _requestNewAuthentication();
    credentialsStore.saveNewAuthData(newAuthentication);

    //Try again with new credentials
    return _callByConfigWithAuth();
  }

  private RESULT _callByConfigNoAuth() throws RequestTimeoutException, RequestFailedException
  {
    try
    {
      return _doCall(callConfig.justCreateCall(), callConfig.getResultType());
    }
    catch (AuthenticationImpossibleException pE)
    {
      throw new InternalCommunicationException("Authentication must not be required!");
    }
  }

  private RESULT _callByConfigWithAuth() throws RequestFailedException, RequestTimeoutException, AuthenticationImpossibleException
  {
    return _doCall(_createCallWithAuthHeader(), callConfig.getResultType());
  }

  private <R> R _doCall(Call pCall, Class<R> pResultType) throws RequestFailedException, RequestTimeoutException,
      AuthenticationImpossibleException
  {
    try (final Response response = pCall.execute())
    {
      if (!response.isSuccessful())
        _doHandleNonSuccessfulResponse(response);

      final ResponseBody body = response.body();
      assert body != null;

      return GSON_INSTANCE.fromJson(body.string(), pResultType);
    }
    catch (IOException pE)
    {
      throw new RequestTimeoutException(timeoutConfig, pE);
    }
  }

  private void _doHandleNonSuccessfulResponse(Response pResponse) throws RequestFailedException, AuthenticationImpossibleException
  {
    final ESupportedHttpStatus status = ESupportedHttpStatus.byCode(pResponse.code());

    switch (status)
    {
      case BAD_REQUEST:
        throw new RequestFailedException(pResponse);
      case NOT_AUTHORIZED:
        throw new AuthenticationImpossibleException();
      case INTERNAL_SERVER_ERROR:
        throw new InternalCommunicationException(pResponse);
      default:
        throw new InternalCommunicationException("Status " + status + " not handled!");
    }
  }

  private SVLAuthenticationResponse _requestNewAuthentication() throws RequestTimeoutException, AuthenticationImpossibleException
  {
    try
    {
      final Call authenticationCall = callConfig.createAuthenticationCall();
      return _doCall(authenticationCall, SVLAuthenticationResponse.class);
    }
    catch (RequestFailedException pE)
    {
      throw new AuthenticationImpossibleException();
    }
  }

  private Call _createCallWithAuthHeader()
  {
    final Request.Builder builder = callConfig.prepareRequest()
        .header("Authorization", "Bearer " + credentialsStore.getActiveToken());

    return callConfig.buildCall(builder);
  }
}
