package com.github.simondan.svl.app;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.github.simondan.svl.app.server.IServer;
import com.github.simondan.svl.app.util.FormModel;
import com.github.simondan.svl.communication.auth.IRegistrationRequest;

import static com.github.simondan.svl.app.util.CommonUtil.newUserName;
import static com.github.simondan.svl.communication.utils.SharedUtils.*;

public class AuthenticationActivity extends AppCompatActivity
{
  private static final int ID_FIRST_NAME = R.id.text_first_name;
  private static final int ID_LAST_NAME = R.id.text_last_name;
  private static final int ID_MAIL = R.id.text_mail;

  private IServer server;
  private FormModel<IRegistrationRequest> registrationFormModel;

  @Override
  protected void onCreate(Bundle pSavedInstanceState)
  {
    super.onCreate(pSavedInstanceState);
    setContentView(R.layout.activity_authentication);

    server = IServer.getForCurrentActivity(this);
    registrationFormModel = FormModel.createForActivity(this, IRegistrationRequest.class)
        .initFieldAddition(ID_FIRST_NAME, "Vorname", IRegistrationRequest::getUserName)
        .requiresLengthBetween(MIN_NAME_LENGTH, MAX_NAME_LENGTH)
        .combineWithField(ID_LAST_NAME, "Nachname")
        .requiresLengthBetween(MIN_NAME_LENGTH, MAX_NAME_LENGTH)
        .doAddFields(pValues -> newUserName(pValues[0], pValues[1]))
        .initStringFieldAddition(ID_MAIL, "Email", IRegistrationRequest::getMailAddress)
        .requiresRegex(VALID_EMAIL_ADDRESS_REGEX)
        .doAddStringField()
        .addButton(R.id.button_create_account, this::_registerUser)
        .addButton(R.id.button_request_code_dialog, this::_openDialog);
  }

  private void _registerUser()
  {
    registrationFormModel.doOrToastUnsatisfied(request -> server.registerUser(request)
        .doOnCompletion(this::_switchToPenaltyActivity)
        .startCall());
  }

  private void _switchToPenaltyActivity()
  {
    final Intent intent = new Intent(this, PenaltyActivity.class);
    startActivity(intent);
  }

  private void _openDialog()
  {
    RestoreAuthenticationDialog restoreAuthenticationDialog = new RestoreAuthenticationDialog();
    restoreAuthenticationDialog.show(getSupportFragmentManager(), "Restore authentication dialog");
  }
}
