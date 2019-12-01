package com.github.simondan.svl.app;

import android.content.Intent;
import android.os.*;
import androidx.appcompat.app.AppCompatActivity;
import com.github.simondan.svl.app.server.IServer;

/**
 * Splash activity to determine initial activity.
 *
 * @author Simon Danner, 23.11.2019
 */
public class SplashActivity extends AppCompatActivity
{
  private static final int SPLASH_DISPLAY_DURATION = 800;

  @Override
  protected void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_splash);

    new Handler().postDelayed(() ->
    {
      final IServer server = IServer.getForCurrentActivity(SplashActivity.this);
      final Class<?> firstActivity = server.isCredentialsStoreInitialized() ? PenaltyActivity.class : AuthenticationActivity.class;

      startActivity(new Intent(getApplicationContext(), firstActivity));
      finish();
    }, SPLASH_DISPLAY_DURATION);
  }
}
