package com.github.simondan.svl.app.communication;

import android.content.Context;
import com.github.simondan.svl.communication.auth.*;
import okhttp3.FormBody;

import java.time.Instant;

/**
 * @author Simon Danner, 09.11.2019
 */
public interface ICredentialsStore
{
  static ICredentialsStore createForContext(Context pContext)
  {
    return new SecurePreferencesCredentialsStore(pContext);
  }

  boolean areUserDataInitialized();

  boolean areCredentialsInitialized();

  String getFirstName();

  String getLastName();

  String getActiveToken();

  FormBody buildCredentialsForm();

  EUserRole getUserRole();

  Instant getLastRestoreCodeTimestamp();

  void setUserData(String pFirstName, String pLastName);

  void saveNewAuthData(AuthenticationResponse pAuthenticationResponse);

  void setLastRestoreCodeTimestamp(Instant pTimestamp);

  void reset();
}
