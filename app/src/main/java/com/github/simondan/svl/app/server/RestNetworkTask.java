package com.github.simondan.svl.app.server;

import android.content.Context;
import android.os.AsyncTask;
import com.github.simondan.svl.app.communication.IRestInterface;
import com.github.simondan.svl.app.communication.exceptions.*;

/**
 * @author Simon Danner, 18.11.2019
 */
class RestNetworkTask<RESULT> extends AsyncTask<Void, Void, RESULT>
{
  private final IRestInterface restInterface;
  private final IRestTaskCallback<RESULT> callback;
  private final RestCall<RESULT> restCall;
  private boolean success;

  RestNetworkTask(Context pContext, IRestTaskCallback<RESULT> pCallback, RestCall<RESULT> pRestCall)
  {
    restInterface = IRestInterface.createForContext(pContext);
    callback = pCallback;
    restCall = pRestCall;
  }

  @Override
  protected void onPreExecute()
  {
    callback.onTaskStart();
  }

  @Override
  protected RESULT doInBackground(Void... pVoids)
  {
    try
    {
      final RESULT result = restCall.call(restInterface);
      success = true;
      return result;
    }
    catch (RequestTimeoutException pE)
    {
      callback.onTimeout();
    }
    catch (AuthenticationImpossibleException pE)
    {
      callback.onAuthenticationImpossible();
    }
    catch (RequestFailedException pE)
    {
      callback.onServerErrorResponse(pE.getServerMessage());
    }

    return null;
  }

  @Override
  protected void onPostExecute(RESULT pResult)
  {
    callback.onTaskEnd();
    if (success)
      callback.onResult(pResult);
  }

  @FunctionalInterface
  interface RestCall<RESULT>
  {
    RESULT call(IRestInterface pRestInterface) throws RequestTimeoutException, RequestFailedException, AuthenticationImpossibleException;
  }

  @FunctionalInterface
  interface VoidRestCall extends RestCall<Void>
  {
    void justCall(IRestInterface pRestInterface) throws RequestTimeoutException, RequestFailedException, AuthenticationImpossibleException;

    @Override
    default Void call(IRestInterface pRestInterface) throws RequestTimeoutException, RequestFailedException, AuthenticationImpossibleException
    {
      justCall(pRestInterface);
      return null;
    }
  }
}
