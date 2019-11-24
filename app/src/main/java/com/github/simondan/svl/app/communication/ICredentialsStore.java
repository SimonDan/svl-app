package com.github.simondan.svl.app.communication;

import android.content.Context;
import com.github.simondan.svl.communication.auth.*;
import okhttp3.FormBody;

/**
 * @author Simon Danner, 09.11.2019
 */
public interface ICredentialsStore
{
  static ICredentialsStore createForContext(Context pContext)
  {
    return new SecurePreferencesCredentialsStore(pContext);
  }

  boolean isInitialized();

  String getActiveToken();

  FormBody buildCredentialsForm();

  EUserRole getUserRole();

  void setUserData(String pFirstName, String pLastName);

  void saveNewAuthData(AuthenticationResponse pAuthenticationResponse);

  void reset();
}
