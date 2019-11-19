package com.github.simondan.svl.app;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDialogFragment;

public class Repeated_Dialog extends AppCompatDialogFragment implements View.OnClickListener {

    EditText editTextMail;
    EditText editTextPassword;
    Button buttonSend;


    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.layout_dialog, null);


        builder.setView(view)
                .setTitle("Login")
                .setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                })
                .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        //Serveranfrage starten
                    }
                });
        editTextMail = view.findViewById(R.id.edit_repeated_loginMail);
        editTextPassword = view.findViewById(R.id.edit_repeated_login_Passwort);

        buttonSend = view.findViewById(R.id.repeated_login_Button_Senden);
        buttonSend.setOnClickListener(this);

        return builder.create();
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.buttonSend:
                Toast.makeText(getApplicationContext(),"Es wurde soeben an die oben genante Mail Adresse ein neues Passwort gesendet", Toast.LENGTH_LONG).show();
                break;
        }
    }


}