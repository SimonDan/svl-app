package com.github.simondan.svl.app.communication;

import android.content.Context;
import com.github.simondan.svl.communication.auth.*;
import de.adito.ojcms.rest.auth.api.AuthenticationRequest;

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

  boolean isUserMailInitialized();

  boolean areCredentialsInitialized();

  String getUserMail();

  String getActiveToken();

  AuthenticationRequest buildAuthenticationRequest();

  EUserRole getUserRole();

  Instant getLastRestoreCodeTimestamp();

  void setUserMail(String pUserMail);

  void saveNewAuthData(SVLAuthenticationResponse pAuthenticationResponse);

  void setLastRestoreCodeTimestamp(Instant pTimestamp);

  void reset();
}
