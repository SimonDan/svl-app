package com.github.simondan.svl.app.server;

import android.app.Activity;
import android.view.Window;
import com.github.simondan.svl.communication.auth.EUserRole;
import de.adito.ojcms.rest.auth.api.RegistrationRequest;

import java.util.Optional;
import java.util.function.Consumer;

/**
 * @author Simon Danner, 16.11.2019
 */
public interface IServer
{
  static IServer getForCurrentActivity(Activity pCurrentActivity)
  {
    return new ServerImpl(pCurrentActivity, pCurrentActivity.getWindow());
  }

  static IServer getForCurrentActivityAndWindow(Activity pCurrentActivity, Window pWindow)
  {
    return new ServerImpl(pCurrentActivity, pWindow);
  }

  boolean isCredentialsStoreInitialized();

  Optional<LastRestoreData> getLastRestoreData();

  EUserRole getUserRole();

  ICompletionCallback registerUser(RegistrationRequest pRegistrationRequest);

  ICompletionCallback requestRestoreCode(String pUserMail);

  ICompletionCallback restoreAuthentication(String pRestoreCode);

  interface IResultCallback<RESULT> extends IStarter
  {
    IStarter doOnResult(Consumer<RESULT> pResultConsumer);
  }

  interface ICompletionCallback extends IStarter
  {
    IStarter doOnCompletion(Runnable pAction);
  }

  @FunctionalInterface
  interface IStarter
  {
    void startCall();
  }
}
