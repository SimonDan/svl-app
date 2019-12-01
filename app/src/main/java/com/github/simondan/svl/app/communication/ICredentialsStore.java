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

  boolean isUserNameInitialized();

  boolean areCredentialsInitialized();

  UserName getUserName();

  String getActiveToken();

  FormBody buildCredentialsForm();

  EUserRole getUserRole();

  Instant getLastRestoreCodeTimestamp();

  void setUserName(UserName pUserName);

  void saveNewAuthData(AuthenticationResponse pAuthenticationResponse);

  void setLastRestoreCodeTimestamp(Instant pTimestamp);

  void reset();
}
