package com.github.simondan.svl.app.server;

import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.widget.ProgressBar;
import com.github.simondan.svl.app.*;
import com.github.simondan.svl.app.communication.*;
import com.github.simondan.svl.app.util.AndroidUtil;
import com.github.simondan.svl.communication.auth.EUserRole;

import java.util.function.Consumer;

/**
 * @author Simon Danner, 18.11.2019
 */
class ServerImpl implements IServer
{
  private final Activity currentActivity;
  private final ICredentialsStore credentialsStore;

  ServerImpl(Activity pCurrentActivity)
  {
    currentActivity = pCurrentActivity;
    credentialsStore = ICredentialsStore.createForContext(currentActivity);
  }

  @Override
  public boolean isCredentialsStoreInitialized()
  {
    return credentialsStore.isInitialized();
  }

  @Override
  public EUserRole getUserRole()
  {
    if (!isCredentialsStoreInitialized())
      throw new IllegalStateException("Credentials store is not initialized yet!");

    return credentialsStore.getUserRole();
  }

  @Override
  public ICompletionCallback registerUser(String pFirstName, String pLastName, String pMail)
  {
    return new _NoResultTask(pRestInterface -> pRestInterface.registerNewUser(pFirstName, pLastName, pMail));
  }

  @Override
  public ICompletionCallback requestAuthRestoreCode(String pFirstName, String pLastName, String pMail)
  {
    return new _NoResultTask(pRestInterface -> pRestInterface.requestAuthRestoreCode(pFirstName, pLastName, pMail));
  }

  @Override
  public ICompletionCallback restoreAuthentication(String pFirstName, String pLastName, String pRestoreCode)
  {
    return new _NoResultTask(pRestInterface -> pRestInterface.restoreAuthentication(pFirstName, pLastName, pRestoreCode));
  }

  @Override
  public IResultCallback<String> retrieveDummy()
  {
    return new _ResultTask<>(IRestInterface::getDummy);
  }

  private class _NoResultTask extends _AbstractTask<Void> implements ICompletionCallback
  {
    _NoResultTask(RestNetworkTask.VoidRestCall pRestCall)
    {
      super(pRestCall);
    }

    @Override
    public IStarter doOnCompletion(Runnable pAction)
    {
      final RestNetworkTask<Void> restTask = createRestTask(pVoid -> pAction.run());
      return restTask::execute;
    }
  }

  private class _ResultTask<RESULT> extends _AbstractTask<RESULT> implements IResultCallback<RESULT>
  {
    _ResultTask(RestNetworkTask.RestCall<RESULT> pRestCall)
    {
      super(pRestCall);
    }

    @Override
    public IStarter doOnResult(Consumer<RESULT> pResultConsumer)
    {
      final RestNetworkTask<RESULT> restTask = createRestTask(pResultConsumer);
      return restTask::execute;
    }
  }

  private abstract class _AbstractTask<RESULT> implements IStarter
  {
    private final RestNetworkTask.RestCall<RESULT> restCall;

    _AbstractTask(RestNetworkTask.RestCall<RESULT> pRestCall)
    {
      restCall = pRestCall;
    }

    RestNetworkTask<RESULT> createRestTask(Consumer<RESULT> pResultConsumer)
    {
      return new RestNetworkTask<>(currentActivity, new _Callback<>(pResultConsumer), restCall);
    }

    @Override
    public void startCall()
    {
      createRestTask(pResult -> {
      }).execute();
    }
  }

  private class _Callback<RESULT> implements IRestTaskCallback<RESULT>
  {
    private final Consumer<RESULT> resultConsumer;

    private _Callback(Consumer<RESULT> pResultConsumer)
    {
      resultConsumer = pResultConsumer;
    }

    @Override
    public void onTaskStart()
    {
      _changeProgressBarVisibility(View.VISIBLE);
    }

    @Override
    public void onTaskEnd()
    {
      _changeProgressBarVisibility(View.INVISIBLE);
    }

    @Override
    public void onTimeout()
    {
      _showToast("Keine Verbindung! Versuche es nochmal!");
    }

    @Override
    public void onAuthenticationImpossible()
    {
      credentialsStore.reset();
      currentActivity.startActivity(new Intent(currentActivity, AuthenticationActivity.class));
      currentActivity.finish();
    }

    @Override
    public void onServerErrorResponse(String pMessage)
    {
      _showToast(pMessage);
    }

    @Override
    public void onResult(RESULT pResult)
    {
      resultConsumer.accept(pResult);
    }

    private void _changeProgressBarVisibility(int pVisibility)
    {
      final ProgressBar progressBar = currentActivity.findViewById(R.id.progress_bar);
      progressBar.setVisibility(pVisibility);
    }

    private void _showToast(String pMessage)
    {
      _onUi(() -> AndroidUtil.showErrorToast(currentActivity, pMessage));
    }

    private void _onUi(Runnable pOnUiAction)
    {
      currentActivity.runOnUiThread(pOnUiAction);
    }
  }
}
