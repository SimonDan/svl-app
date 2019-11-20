package com.github.simondan.svl.app;

import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.util.Log;
import android.view.*;

public class BeginActivity extends AppCompatActivity {
  private static final String TAG= "xx SignInActivity";

  @Override
  protected void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_begin);
    Toolbar toolbar = findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);

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
  public boolean onCreateOptionsMenu(Menu menu) {
    MenuInflater inflator = getMenuInflater();
    inflator.inflate(R.menu.menu_main, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
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
