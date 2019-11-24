package com.github.simondan.svl.app;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.github.simondan.svl.app.server.IServer;
import com.github.simondan.svl.app.util.FormModel;

public class AuthenticationActivity extends AppCompatActivity
{
  private static final int ID_FIRST_NAME = R.id.text_first_name;
  private static final int ID_LAST_NAME = R.id.text_last_name;
  private static final int ID_MAIL = R.id.text_mail;

  private IServer server;
  private FormModel formModel;

  @Override
  protected void onCreate(Bundle pSavedInstanceState)
  {
    super.onCreate(pSavedInstanceState);
    setContentView(R.layout.activity_authentication);

    server = IServer.getForCurrentActivity(this);
    formModel = FormModel.createForActivity(this)
        .addEditText(ID_FIRST_NAME, "Vorname")
        .addEditText(ID_LAST_NAME, "Nachname")
        .addEditText(ID_MAIL, "Email")
        .addButton(R.id.button_create_account, this::_registerUser)
        .addButton(R.id.button_request_code_dialog, this::_openDialog);
  }

  private void _registerUser()
  {
    if (formModel.allSatisfied())
      server.registerUser(formModel.value(ID_FIRST_NAME), formModel.value(ID_LAST_NAME), formModel.value(ID_MAIL))
          .doOnCompletion(this::_switchToPenaltyActivity)
          .startCall();
    else
      formModel.toastAllUnsatisfied();
  }

  private void _switchToPenaltyActivity()
  {
    final Intent intent = new Intent(this, PenaltyActivity.class);
    startActivity(intent);
  }

  private void _openDialog()
  {
    RestoreAuthenticationDialog restoreAuthenticationDialog = new RestoreAuthenticationDialog();
    restoreAuthenticationDialog.show(getSupportFragmentManager(), "Server dialog");
  }
}
