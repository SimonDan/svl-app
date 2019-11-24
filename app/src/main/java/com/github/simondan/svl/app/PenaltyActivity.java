package com.github.simondan.svl.app;

import android.os.Bundle;
import android.util.Log;
import android.view.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.github.simondan.svl.app.server.IServer;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

public class PenaltyActivity extends AppCompatActivity
{
  private static final String TAG = "xx SignInActivity";

  private IServer server;

  @Override
  protected void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_penalty);
    Toolbar toolbar = findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);

    server = IServer.getForCurrentActivity(this);
    server.retrieveDummy()
        .doOnResult(System.out::println) //Just check if we got auth
        .startCall();

    FloatingActionButton fab = findViewById(R.id.fab);
    fab.setOnClickListener(new View.OnClickListener()
    {
      @Override
      public void onClick(View view)
      {
        Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
            .setAction("Action", null).show();
      }
    });
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu)
  {
    MenuInflater inflator = getMenuInflater();
    inflator.inflate(R.menu.menu_main, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item)
  {
    switch (item.getItemId())
    {
      case R.id.mainMenuStrafenAnlegen:
        Log.d(TAG, "Strafen");
        return true;

      case R.id.mainMenuBenutzerVerwalten:
        Log.d(TAG, "Benutzer");
        return true;

      default:
        return super.onOptionsItemSelected(item);
    }
  }
}
