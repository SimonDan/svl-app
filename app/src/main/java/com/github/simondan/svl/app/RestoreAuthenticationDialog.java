package com.github.simondan.svl.app;

import android.app.*;
import android.content.Intent;
import android.os.*;
import android.view.*;
import android.widget.*;
import androidx.annotation.*;
import androidx.appcompat.app.AppCompatDialogFragment;
import androidx.core.content.ContextCompat;
import com.github.simondan.svl.app.server.*;
import com.github.simondan.svl.app.util.SingleStringFormModel;

import java.time.*;
import java.util.Objects;
import java.util.function.Function;

import static de.adito.ojcms.rest.auth.util.SharedUtils.*;

public class RestoreAuthenticationDialog extends AppCompatDialogFragment
{
  private static final int ID_MAIL = R.id.edit_restore_auth_mail;
  private static final int ID_CODE = R.id.edit_restore_auth_code;

  private IServer server;
  private SingleStringFormModel formSendCode;
  private SingleStringFormModel formRestore;
  private View dialogView;
  private CountDownTimer currentCountDown;

  @NonNull
  @Override
  public Dialog onCreateDialog(@Nullable Bundle savedInstanceState)
  {
    final LayoutInflater inflater = Objects.requireNonNull(getActivity()).getLayoutInflater();
    dialogView = inflater.inflate(R.layout.restore_dialog, null);

    final AlertDialog dialog = new AlertDialog.Builder(getActivity(), R.style.DialogStyle)
        .setView(dialogView)
        .setTitle("Account Wiederherstellung")
        .setNegativeButton("Schließen", (dialogInterface, i) -> dialogInterface.dismiss())
        .create();

    server = IServer.getForCurrentActivityAndWindow(getActivity(), dialog.getWindow());

    formSendCode = SingleStringFormModel.createForView(dialogView, dialog.getWindow())
        .configureSingleStringFieldAddition(ID_MAIL, "Email")
        .requiresRegex(VALID_EMAIL_ADDRESS_REGEX)
        .doAddSingleStringField()
        .addButton(R.id.button_send_code, this::_sendRestoreCode);

    formRestore = SingleStringFormModel.createForView(dialogView, dialog.getWindow())
        .configureSingleStringFieldAddition(ID_CODE, "Code")
        .requiresExactLength(CODE_LENGTH)
        .doAddSingleStringField()
        .addButton(R.id.button_restore_account, this::_restoreAuthentication);

    server.getLastRestoreData().ifPresent(this::_enableRestoreButton);

    dialog.setOnShowListener(arg ->
    {
      //Set button color
      dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(ContextCompat.getColor(dialog.getContext(), R.color.svl_yellow));
    });

    return dialog;
  }

  private void _enableRestoreButton(LastRestoreData pRestoreData)
  {
    final Instant expirationTimestamp = pRestoreData.getSendTimestamp().plus(RESTORE_CODE_EXPIRATION_THRESHOLD);
    final TextView codeText = dialogView.findViewById(R.id.text_code);

    final String textInvalidCode = "Der Code für " + pRestoreData.getUserMail() + " ist abgelaufen und nicht mehr gültig!";

    if (expirationTimestamp.isBefore(Instant.now()))
    {
      codeText.setText(textInvalidCode);
      return;
    }

    dialogView.findViewById(R.id.edit_restore_auth_code).setEnabled(true);
    dialogView.findViewById(R.id.button_restore_account).setEnabled(true);

    final Function<Duration, String> textCreator = pRemaining -> "Ein Code wurde für " + pRestoreData.getUserMail() +
        " gesendet!\nEr ist noch " + pRemaining.toMinutes() + " Minuten und " + pRemaining.getSeconds() % 60 + " Sekunden gültig!";

    if (currentCountDown != null)
      currentCountDown.cancel();

    currentCountDown = new CountDownTimer(Duration.between(Instant.now(), expirationTimestamp).toMillis(), 1000)
    {
      @Override
      public void onTick(long pMillisUntilFinished)
      {
        codeText.setText(textCreator.apply(Duration.ofMillis(pMillisUntilFinished)));
      }

      @Override
      public void onFinish()
      {
        codeText.setText(textInvalidCode);
        dialogView.findViewById(R.id.edit_restore_auth_code).setEnabled(false);
        dialogView.findViewById(R.id.button_restore_account).setEnabled(false);
      }
    }.start();
  }

  private void _sendRestoreCode()
  {
    formSendCode.doOrToastUnsatisfied(pRequest -> server.requestRestoreCode(pRequest)
        .doOnCompletion(() ->
        {
          Toast.makeText(getActivity(), "Wiederherstellungscode per Email versandt!", Toast.LENGTH_LONG).show();
          server.getLastRestoreData().ifPresent(this::_enableRestoreButton);
        })
        .startCall());
  }

  private void _restoreAuthentication()
  {
    formRestore.doOrToastUnsatisfied(pCode -> server.restoreAuthentication(pCode)
        .doOnCompletion(() ->
        {
          dismiss();
          Intent intent = new Intent(getContext(), PenaltyActivity.class);
          startActivity(intent);
        })
        .startCall());
  }
}