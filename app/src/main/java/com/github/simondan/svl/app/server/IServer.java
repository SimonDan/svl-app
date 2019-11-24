package com.github.simondan.svl.app.server;

import android.app.Activity;
import com.github.simondan.svl.communication.auth.EUserRole;

import java.util.function.Consumer;

/**
 * @author Simon Danner, 16.11.2019
 */
public interface IServer
{
  static IServer getForCurrentActivity(Activity pCurrentActivity)
  {
    return new ServerImpl(pCurrentActivity);
  }

  boolean isCredentialsStoreInitialized();

  EUserRole getUserRole();

  ICompletionCallback registerUser(String pFirstName, String pLastName, String pMail);

  ICompletionCallback requestAuthRestoreCode(String pFirstName, String pLastName, String pMail);

  ICompletionCallback restoreAuthentication(String pFirstName, String pLastName, String pRestoreCode);

  IResultCallback<String> retrieveDummy();

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
