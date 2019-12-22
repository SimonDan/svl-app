package com.github.simondan.svl.app.server;

import android.app.Activity;
import android.content.Intent;
import android.view.*;
import com.github.simondan.svl.app.*;
import com.github.simondan.svl.app.communication.*;
import com.github.simondan.svl.app.communication.exceptions.InternalCommunicationException;
import com.github.simondan.svl.communication.auth.*;
import org.jetbrains.annotations.Nullable;

import java.time.Instant;
import java.util.Optional;
import java.util.function.Consumer;

import static com.github.simondan.svl.app.util.AndroidUtil.showError;

/**
 * @author Simon Danner, 18.11.2019
 */
class ServerImpl implements IServer
{
  private final Activity currentActivity;
  private final Window currentWindow;
  private final ICredentialsStore credentialsStore;

  ServerImpl(Activity pCurrentActivity, Window pCurrentWindow)
  {
    currentActivity = pCurrentActivity;
    currentWindow = pCurrentWindow;
    credentialsStore = ICredentialsStore.createForContext(currentActivity);
  }

  @Override
  public boolean isCredentialsStoreInitialized()
  {
    return credentialsStore.areCredentialsInitialized();
  }

  @Override
  public Optional<LastRestoreData> getLastRestoreData()
  {
    if (!credentialsStore.areCredentialsInitialized() && credentialsStore.isUserNameInitialized())
    {
      final UserName userName = credentialsStore.getUserName();
      final Instant timestamp = credentialsStore.getLastRestoreCodeTimestamp();

      return Optional.of(new LastRestoreData(userName, timestamp));
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
  public ICompletionCallback registerUser(IRegistrationRequest pRegistrationRequest)
  {
    return new _NoResultTask(pRestInterface -> pRestInterface.registerNewUser(pRegistrationRequest));
  }

  @Override
  public ICompletionCallback requestRestoreCode(IRegistrationRequest pRegistrationData)
  {
    final Runnable storeUserData = () ->
    {
      credentialsStore.setUserName(pRegistrationData.getUserName());
      credentialsStore.setLastRestoreCodeTimestamp(Instant.now());
    };

    return new _NoResultTask(pRestInterface -> pRestInterface.requestAuthRestoreCode(pRegistrationData), storeUserData);
  }

  @Override
  public ICompletionCallback restoreAuthentication(String pRestoreCode)
  {
    if (!credentialsStore.isUserNameInitialized())
      throw new InternalCommunicationException("No user data set for authentication recovery!");

    final IRestoreAuthRequest request = new IRestoreAuthRequest()
    {
      @Override
      public UserName getUserName()
      {
        return credentialsStore.getUserName();
      }

      @Override
      public String getRestoreCode()
      {
        return pRestoreCode;
      }
    };

    return new _NoResultTask(pRestInterface -> pRestInterface.restoreAuthentication(request));
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
      createRestTask(pResult ->
      {
      }).execute();
    }
  }

  private class _Callback<RESULT> implements IRestTaskCallback<RESULT>
  {
    private final Consumer<RESULT> resultConsumer;
    private int priorVisibility;

    private _Callback(Consumer<RESULT> pResultConsumer)
    {
      resultConsumer = pResultConsumer;
    }

    @Override
    public void onTaskStart()
    {
      final View progressOverlay = currentWindow.findViewById(R.id.progress_overlay);
      priorVisibility = progressOverlay.getVisibility();
      progressOverlay.setVisibility(View.VISIBLE);
    }

    @Override
    public void onTaskEnd()
    {
      final View progressOverlay = currentWindow.findViewById(R.id.progress_overlay);
      progressOverlay.setVisibility(priorVisibility);
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

    private void _showToast(String pMessage)
    {
      _onUi(() -> showError(currentWindow.findViewById(android.R.id.content), pMessage));
    }

    private void _onUi(Runnable pOnUiAction)
    {
      currentActivity.runOnUiThread(pOnUiAction);
    }
  }
}
