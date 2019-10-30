package com.github.simondan.svl.app;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class RepeatedLogin extends AppCompatActivity implements View.OnClickListener {

    Button mButton;
    Button mButton2;
    EditText mEdittext;
    EditText mEdittext2;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_repeated_login);

        mEdittext = (EditText) findViewById(R.id.activity_repeated_login_EditText_Mail);
        mEdittext2 = (EditText) findViewById(R.id.activity_repeated_login_Passwort);
        mButton = (Button) findViewById(R.id.activity_repeated_login_Button_Senden);
        mButton2 = (Button) findViewById(R.id.activity_repeated_login_Button_Senden_Server);


        mButton.setOnClickListener(this);
        mButton2.setOnClickListener(this);
        mEdittext.setOnClickListener(this);
        mEdittext2.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        switch (i) {
            case R.id.activity_repeated_login_Button_Senden:
                Toast.makeText(getApplicationContext(),
                        "Sie haben soeben ein Passwort an diese Mailadresse erhalten", Toast.LENGTH_LONG).show();
                return;
            case R.id.activity_repeated_login_Button_Senden_Server:
                Toast.makeText(getApplicationContext(),
                        "Das Passwort wurde an den Server erfolgreich gesendet. Starten Sie die App neu.", Toast.LENGTH_LONG).show();
                return;


        }
    }
}
