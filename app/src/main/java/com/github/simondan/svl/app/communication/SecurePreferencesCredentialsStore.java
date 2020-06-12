package com.github.simondan.svl.app.communication;

import android.content.*;
import androidx.security.crypto.*;
import com.github.simondan.svl.communication.auth.*;
import de.adito.ojcms.rest.auth.api.AuthenticationRequest;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.Instant;
import java.util.Optional;

import static com.github.simondan.svl.communication.auth.SVLAuthenticationResponse.USER_ROLE;
import static de.adito.ojcms.rest.auth.api.AuthenticationResponse.*;

/**
 * @author Simon Danner, 16.11.2019
 */
final class SecurePreferencesCredentialsStore implements ICredentialsStore
{
  private static final String TOKEN_KEY = "tokenKey";
  private static final String USER_MAIL_KEY = "userMailKey";
  private static final String NEXT_PASSWORD_KEY = "nextPasswordKey";
  private static final String USER_ROLE_KEY = "nextPasswordKey";
  private static final String RESTORE_TIMESTAMP_KEY = "restoreTimestampKey";
  private static final String MASTER_KEY;

  private final SharedPreferences sharedPreferences;

  static
  {
    try
    {
      MASTER_KEY = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC);
    }
    catch (GeneralSecurityException | IOException pE)
    {
      throw new RuntimeException(pE);
    }
  }

  SecurePreferencesCredentialsStore(Context pContext)
  {
    try
    {
      sharedPreferences = EncryptedSharedPreferences.create(
          "secret_shared_prefs",
          MASTER_KEY,
          pContext,
          EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
          EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
      );
    }
    catch (GeneralSecurityException | IOException pE)
    {
      throw new RuntimeException(pE);
    }
  }

  @Override
  public boolean isUserMailInitialized()
  {
    return sharedPreferences.contains(USER_MAIL_KEY);
  }

  @Override
  public boolean areCredentialsInitialized()
  {
    return sharedPreferences.contains(USER_MAIL_KEY) &&
        sharedPreferences.contains(NEXT_PASSWORD_KEY) &&
        sharedPreferences.contains(TOKEN_KEY);
  }

  @Override
  public String getUserMail()
  {
    return _read(USER_MAIL_KEY);
  }

  @Override
  public String getActiveToken()
  {
    return _read(TOKEN_KEY);
  }

  @Override
  public AuthenticationRequest buildAuthenticationRequest()
  {
    final String userMail = getUserMail();
    return new AuthenticationRequest(userMail, _read(NEXT_PASSWORD_KEY));
  }

  @Override
  public EUserRole getUserRole()
  {
    return EUserRole.valueOf(_read(USER_ROLE_KEY));
  }

  @Override
  public Instant getLastRestoreCodeTimestamp()
  {
    return Instant.ofEpochMilli(Long.parseLong(_read(RESTORE_TIMESTAMP_KEY)));
  }

  @Override
  public void setUserMail(String pUserMail)
  {
    final SharedPreferences.Editor editor = sharedPreferences.edit();
    editor.putString(USER_MAIL_KEY, pUserMail);
    editor.apply();
  }

  @Override
  public void saveNewAuthData(SVLAuthenticationResponse pAuthenticationResponse)
  {
    final SharedPreferences.Editor editor = sharedPreferences.edit();

    editor.putString(TOKEN_KEY, pAuthenticationResponse.getValue(TOKEN));
    editor.putString(NEXT_PASSWORD_KEY, pAuthenticationResponse.getValue(NEXT_PASSWORD));
    editor.putString(USER_ROLE_KEY, pAuthenticationResponse.getValue(USER_ROLE).name());
    editor.apply();
  }

  @Override
  public void setLastRestoreCodeTimestamp(Instant pTimestamp)
  {
    final SharedPreferences.Editor editor = sharedPreferences.edit();
    editor.putString(RESTORE_TIMESTAMP_KEY, String.valueOf(pTimestamp.toEpochMilli()));
    editor.apply();
  }

  @Override
  public void reset()
  {
    final SharedPreferences.Editor editor = sharedPreferences.edit();

    editor.remove(USER_MAIL_KEY);
    editor.remove(TOKEN_KEY);
    editor.remove(NEXT_PASSWORD_KEY);
    editor.remove(USER_ROLE_KEY);
    editor.apply();
  }

  private String _read(final String pKey)
  {
    return Optional.ofNullable(sharedPreferences.getString(pKey, null))
        .orElseThrow(() -> new RuntimeException("No value for key " + pKey));
  }
}
