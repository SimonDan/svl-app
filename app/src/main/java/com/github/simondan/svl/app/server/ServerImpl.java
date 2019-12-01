package com.github.simondan.svl.app.server;

import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.widget.ProgressBar;
import com.github.simondan.svl.app.*;
import com.github.simondan.svl.app.communication.*;
import com.github.simondan.svl.app.communication.exceptions.InternalCommunicationException;
import com.github.simondan.svl.app.util.AndroidUtil;
import com.github.simondan.svl.communication.auth.EUserRole;
import org.jetbrains.annotations.Nullable;

import java.time.Instant;
import java.util.Optional;
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
    return credentialsStore.areCredentialsInitialized();
  }

  @Override
  public Optional<LastRestoreCode> getLastRestoreCodeData()
  {
    if (!credentialsStore.areCredentialsInitialized() && credentialsStore.areUserDataInitialized())
    {
      final String firstName = credentialsStore.getFirstName();
      final String lastName = credentialsStore.getLastName();
      final Instant timestamp = credentialsStore.getLastRestoreCodeTimestamp();

      return Optional.of(new LastRestoreCode(firstName, lastName, timestamp));
    }

    return Optional.empty();
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
  public ICompletionCallback requestRestoreCode(String pFirstName, String pLastName, String pMail)
  {
    final Runnable storeUserData = () ->
    {
      credentialsStore.setUserData(pFirstName, pLastName);
      credentialsStore.setLastRestoreCodeTimestamp(Instant.now());
    };

    return new _NoResultTask(pRestInterface -> pRestInterface.requestAuthRestoreCode(pFirstName, pLastName, pMail), storeUserData);
  }

  @Override
  public ICompletionCallback restoreAuthentication(String pRestoreCode)
  {
    if (!credentialsStore.areUserDataInitialized())
      throw new InternalCommunicationException("No user data set for authentication recovery!");

    final String firstName = credentialsStore.getFirstName();
    final String lastName = credentialsStore.getLastName();

    return new _NoResultTask(pRestInterface -> pRestInterface.restoreAuthentication(firstName, lastName, pRestoreCode));
  }

  @Override
  public IResultCallback<String> retrieveDummy()
  {
    return new _ResultTask<>(IRestInterface::getDummy);
  }

  private class _NoResultTask extends _AbstractTask<Void> implements ICompletionCallback
  {
    private final Runnable internalCompletionAction;

    _NoResultTask(RestNetworkTask.VoidRestCall pRestCall)
    {
      this(pRestCall, null);
    }

    _NoResultTask(RestNetworkTask.VoidRestCall pRestCall, @Nullable Runnable pInternalCompletionAction)
    {
      super(pRestCall);
      internalCompletionAction = pInternalCompletionAction;
    }

    @Override
    public IStarter doOnCompletion(Runnable pAction)
    {
      final RestNetworkTask<Void> restTask = createRestTask(pVoid ->
                                                            {
                                                              if (internalCompletionAction != null)
                                                                internalCompletionAction.run();
                                                              pAction.run();
                                                            });
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
