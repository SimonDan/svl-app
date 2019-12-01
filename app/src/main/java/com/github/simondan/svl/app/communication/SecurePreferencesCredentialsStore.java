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
  private static final String USER_NAME_KEY = "userNameKey";
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
  public boolean isUserNameInitialized()
  {
    return sharedPreferences.contains(USER_NAME_KEY);
  }

  @Override
  public boolean areCredentialsInitialized()
  {
    return sharedPreferences.contains(USER_NAME_KEY) &&
        sharedPreferences.contains(NEXT_PASSWORD_KEY) &&
        sharedPreferences.contains(TOKEN_KEY);
  }

  @Override
  public UserName getUserName()
  {
    try
    {
      return UserName.of(_read(USER_NAME_KEY));
    }
    catch (BadUserNameException pE)
    {
      throw new RuntimeException(pE);
    }
  }

  @Override
  public String getActiveToken()
  {
    return _read(TOKEN_KEY);
  }

  @Override
  public FormBody buildCredentialsForm()
  {
    final UserName userName = getUserName();

    return new FormBody.Builder()
        .add(AUTH_FORM_FIRST_NAME, userName.getFirstName())
        .add(AUTH_FORM_LAST_NAME, userName.getLastName())
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
  public void setUserName(UserName pUserName)
  {
    final SharedPreferences.Editor editor = sharedPreferences.edit();
    editor.putString(USER_NAME_KEY, pUserName.toString());
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

    editor.remove(USER_NAME_KEY);
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
