package com.github.simondan.svl.app.server;

import android.app.Activity;
import com.github.simondan.svl.communication.auth.EUserRole;

import java.time.Instant;
import java.util.Optional;
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

  Optional<LastRestoreCode> getLastRestoreCodeData();

  EUserRole getUserRole();

  ICompletionCallback registerUser(String pFirstName, String pLastName, String pMail);

  ICompletionCallback requestRestoreCode(String pFirstName, String pLastName, String pMail);

  ICompletionCallback restoreAuthentication(String pRestoreCode);

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

  class LastRestoreCode
  {
    private final String firstName;
    private final String lastName;
    private final Instant timestamp;

    LastRestoreCode(String pFirstName, String pLastName, Instant pTimestamp)
    {
      firstName = pFirstName;
      lastName = pLastName;
      timestamp = pTimestamp;
    }

    public String getFirstName()
    {
      return firstName;
    }

    public String getLastName()
    {
      return lastName;
    }

    public Instant getSendTimestamp()
    {
      return timestamp;
    }
  }
}
