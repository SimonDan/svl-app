package com.github.simondan.svl.app;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.github.simondan.svl.app.server.IServer;
import com.github.simondan.svl.app.util.FormModel;
import de.adito.ojcms.rest.auth.api.RegistrationRequest;
import de.adito.ojcms.rest.auth.util.SharedUtils;

import static de.adito.ojcms.rest.auth.api.RegistrationRequest.*;

public class AuthenticationActivity extends AppCompatActivity
{
  private static final int MIN_NAME_LENGTH = 3;
  private static final int MAX_NAME_LENGTH = 25;

  private static final int ID_FIRST_NAME = R.id.text_first_name;
  private static final int ID_LAST_NAME = R.id.text_last_name;
  private static final int ID_MAIL = R.id.text_mail;

  private IServer server;
  private FormModel<RegistrationRequest> registrationFormModel;

  @Override
  protected void onCreate(Bundle pSavedInstanceState)
  {
    super.onCreate(pSavedInstanceState);
    setContentView(R.layout.activity_authentication);

    server = IServer.getForCurrentActivity(this);
    registrationFormModel = FormModel.createForActivity(this, RegistrationRequest.class)
        .configureFieldAddition(ID_FIRST_NAME, "Vorname", DISPLAY_NAME)
        .requiresLengthBetween(MIN_NAME_LENGTH, MAX_NAME_LENGTH)
        .combineWithField(ID_LAST_NAME, "Nachname")
        .requiresLengthBetween(MIN_NAME_LENGTH, MAX_NAME_LENGTH)
        .doAddFields(pValues -> pValues[0] + pValues[1])
        .configureStringFieldAddition(ID_MAIL, "Email", USER_MAIL)
        .requiresRegex(SharedUtils.VALID_EMAIL_ADDRESS_REGEX)
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
