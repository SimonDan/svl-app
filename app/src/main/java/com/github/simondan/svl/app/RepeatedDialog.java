package com.github.simondan.svl.app;

import android.app.*;
import android.content.*;
import android.graphics.Color;
import android.os.Bundle;
import android.view.*;
import android.widget.*;
import androidx.annotation.*;
import androidx.appcompat.app.AppCompatDialogFragment;


public class RepeatedDialog extends AppCompatDialogFragment
{
  private EditText editTextMail;
  private EditText editTextPassword;
  private Button buttonSend;

  @NonNull
  @Override
  public Dialog onCreateDialog(@Nullable Bundle savedInstanceState)
  {
    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

    LayoutInflater inflater = getActivity().getLayoutInflater();
    View view = inflater.inflate(R.layout.layout_dialog, null);


   builder.setView(view)
        .setTitle("Login");


        final AlertDialog dialogNegativ = builder.setNegativeButton("cancel", new DialogInterface.OnClickListener()
        {
          @Override
          public void onClick(DialogInterface dialogInterface, int i)
          {

          }

        }).create();

        final AlertDialog dialogPositiv = builder.setPositiveButton("ok", new DialogInterface.OnClickListener()
        {
          @Override
          public void onClick(DialogInterface dialogInterface, int i)
          {
            Intent intent = new Intent(getContext(), BeginActivity.class);
            startActivity(intent);
          }
        }).create();


    //2. now setup to change color of the button
    dialogPositiv.setOnShowListener( new DialogInterface.OnShowListener() {

      @Override
      public void onShow(DialogInterface arg0) {
        dialogPositiv.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.blue());
      }
    });


    editTextMail = view.findViewById(R.id.edit_repeated_loginMail);
    editTextPassword = view.findViewById(R.id.edit_repeated_login_Passwort);

    buttonSend = view.findViewById(R.id.repeated_login_Button_Senden);
    buttonSend.setOnClickListener(new View.OnClickListener()
    {
      @Override
      public void onClick(View v)
      {
        Toast.makeText(getActivity(), "Es wurde soeben an die oben genante Mail Adresse Wiederherstellungs-Code gesendet", Toast.LENGTH_LONG).show();
      }
    });



  return builder.create();
  }
}