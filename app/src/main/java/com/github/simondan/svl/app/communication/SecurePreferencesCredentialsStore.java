package com.github.simondan.svl.app.communication;

import android.content.*;
import androidx.security.crypto.*;
import com.github.simondan.svl.communication.auth.*;
import okhttp3.FormBody;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.Instant;
import java.util.Optional;

import static com.github.simondan.svl.app.communication.config.DefaultConfig.*;

/**
 * @author Simon Danner, 16.11.2019
 */
final class SecurePreferencesCredentialsStore implements ICredentialsStore
{
  private static final String TOKEN_KEY = "tokenKey";
  private static final String FIRST_NAME_KEY = "firstNameKey";
  private static final String LAST_NAME_KEY = "lastNameKey";
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
  public boolean areUserDataInitialized()
  {
    return sharedPreferences.contains(FIRST_NAME_KEY) && sharedPreferences.contains(LAST_NAME_KEY);
  }

  @Override
  public boolean areCredentialsInitialized()
  {
    return sharedPreferences.contains(FIRST_NAME_KEY) &&
        sharedPreferences.contains(LAST_NAME_KEY) &&
        sharedPreferences.contains(NEXT_PASSWORD_KEY) &&
        sharedPreferences.contains(TOKEN_KEY);
  }

  @Override
  public String getFirstName()
  {
    return _read(FIRST_NAME_KEY);
  }

  @Override
  public String getLastName()
  {
    return _read(LAST_NAME_KEY);
  }

  @Override
  public String getActiveToken()
  {
    return _read(TOKEN_KEY);
  }

  @Override
  public FormBody buildCredentialsForm()
  {
    return new FormBody.Builder()
        .add(AUTH_FORM_FIRST_NAME, _read(FIRST_NAME_KEY))
        .add(AUTH_FORM_LAST_NAME, _read(LAST_NAME_KEY))
        .add(AUTH_FORM_PASSWORD, _read(NEXT_PASSWORD_KEY))
        .build();
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
  public void setUserData(String pFirstName, String pLastName)
  {
    final SharedPreferences.Editor editor = sharedPreferences.edit();

    editor.putString(FIRST_NAME_KEY, pFirstName);
    editor.putString(LAST_NAME_KEY, pLastName);
    editor.apply();
  }

  @Override
  public void saveNewAuthData(AuthenticationResponse pAuthenticationResponse)
  {
    final SharedPreferences.Editor editor = sharedPreferences.edit();

    editor.putString(TOKEN_KEY, pAuthenticationResponse.getToken());
    editor.putString(NEXT_PASSWORD_KEY, pAuthenticationResponse.getNextPassword());
    editor.putString(USER_ROLE_KEY, pAuthenticationResponse.getUserRole().name());
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

    editor.remove(FIRST_NAME_KEY);
    editor.remove(LAST_NAME_KEY);
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
