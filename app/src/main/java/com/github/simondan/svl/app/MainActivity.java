package com.github.simondan.svl.app;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "xx Main Activity";

    //UI Variablen Deklarieren
    EditText mEditTextVorName;
    EditText mEditTextNachName;
    EditText mEditTextMail;
    Button mButtonCreateAccount;
    Button mButtonCreateAccount2;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        //UI Variablen initialisieren
        mEditTextVorName = (EditText) findViewById(R.id.main_create_Account_EditText_VorName);
        mEditTextNachName = (EditText) findViewById(R.id.main_create_Account_EditText_NachName);
        mEditTextMail = (EditText) findViewById(R.id.main_create_Account_EditText_Mail);
        mButtonCreateAccount = (Button) findViewById(R.id.main_create_Account_Button);
        mButtonCreateAccount2 = (Button) findViewById(R.id.main_create_Account_Button2);

        //Listener registrieren
        mButtonCreateAccount.setOnClickListener(this);
        mButtonCreateAccount2.setOnClickListener(this);


        //FloatingActionButton fab = findViewById(R.id.fab);
        //fab.setOnClickListener(new View.OnClickListener() {
        //  @Override
        // public void onClick(View view) {
        //    Snackbar.make(view, "Hier geht es zu den Strafen", Snackbar.LENGTH_LONG)
        //            .setAction("Action", null).show();
        //  }
        // });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflator = getMenuInflater();
        inflator.inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "onStop");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.d(TAG, "onRestart called");
    }


    @Override
    public void onClick(View v) {
        int i = v.getId();
        switch (i) {
            case R.id.main_create_Account_Button:
                Toast.makeText(getApplicationContext(),
                        "Create Account pressed", Toast.LENGTH_LONG).show();
                return;
            case R.id.main_create_Account_Button2:
                Intent intent = new Intent(this, RepeatedLogin.class);
                startActivity(intent);
                return;
        }
    }


}
