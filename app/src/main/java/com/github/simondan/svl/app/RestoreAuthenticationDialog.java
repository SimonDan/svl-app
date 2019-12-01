package com.github.simondan.svl.app;

import android.app.*;
import android.content.Intent;
import android.os.Bundle;
import android.view.*;
import android.widget.*;
import androidx.annotation.*;
import androidx.appcompat.app.AppCompatDialogFragment;
import com.github.simondan.svl.app.server.IServer;
import com.github.simondan.svl.app.util.FormModel;
import com.github.simondan.svl.communication.utils.SharedUtils;

import java.util.Objects;

import static com.github.simondan.svl.communication.utils.SharedUtils.*;

public class RestoreAuthenticationDialog extends AppCompatDialogFragment
{
  private static final int ID_FIRST_NAME = R.id.edit_restore_auth_first_name;
  private static final int ID_LAST_NAME = R.id.edit_restore_auth_last_name;
  private static final int ID_MAIL = R.id.edit_restore_auth_mail;
  private static final int ID_CODE = R.id.edit_restore_auth_code;

  private IServer server;
  private FormModel formSendCode;
  private FormModel formRestore;

  @NonNull
  @Override
  public Dialog onCreateDialog(@Nullable Bundle savedInstanceState)
  {
    server = IServer.getForCurrentActivity(getActivity());

    final LayoutInflater inflater = Objects.requireNonNull(getActivity()).getLayoutInflater();
    final View view = inflater.inflate(R.layout.layout_dialog, null);

    final AlertDialog dialog = new AlertDialog.Builder(getActivity())
        .setView(view)
        .setTitle("Account Wiederherstellung")
        .setNegativeButton("cancel", (dialogInterface, i) -> dialogInterface.dismiss())
        .setPositiveButton("ok", null)
        .create();

    formSendCode = FormModel.createForView(view, dialog.getWindow())
        .addEditText(ID_FIRST_NAME, "Vorname", MIN_NAME_LENGTH, MAX_NAME_LENGTH)
        .addEditText(ID_LAST_NAME, "Nachname", MIN_NAME_LENGTH, MAX_NAME_LENGTH)
        .addEditText(ID_MAIL, "Email", SharedUtils.VALID_EMAIL_ADDRESS_REGEX)
        .addButton(R.id.button_send_code, this::_sendRestoreCode);

    formRestore = FormModel.createForView(view, dialog.getWindow())
        .addEditText(ID_CODE, "Code", CODE_LENGTH);

    dialog.setOnShowListener(arg -> {
      final Button okButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);

      okButton.setOnClickListener(view1 -> {
        if (formRestore.allSatisfied())
          _restoreAuthentication();
        else
          formRestore.toastAllUnsatisfied();
      });

      //Set button colors
      okButton.setTextColor(getResources().getColor(R.color.dialogPositiveButton));
      dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(getResources().getColor(R.color.dialogNegativeButton));
    });

    return dialog;
  }

  private void _sendRestoreCode()
  {
    if (!formSendCode.allSatisfied())
      formSendCode.toastAllUnsatisfied();
    else
    {
      server.requestAuthRestoreCode(formSendCode.value(ID_FIRST_NAME), formSendCode.value(ID_LAST_NAME), formSendCode.value(ID_MAIL))
          .doOnCompletion(this::_showSuccessfulToast)
          .startCall();
    }
  }

  private void _restoreAuthentication()
  {
    server.restoreAuthentication(formSendCode.value(ID_FIRST_NAME), formSendCode.value(ID_LAST_NAME), formRestore.value(ID_CODE))
        .doOnCompletion(() ->
                        {
                          dismiss();
                          Intent intent = new Intent(getContext(), PenaltyActivity.class);
                          startActivity(intent);
                        })
        .startCall();
  }

  private void _showSuccessfulToast()
  {
    Toast.makeText(getActivity(), "Wiederherstellungscode per Email versandt!", Toast.LENGTH_LONG).show();
  }
}