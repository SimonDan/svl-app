package com.github.simondan.svl.app.util;

import android.view.View;
import android.widget.TextView;
import androidx.core.content.ContextCompat;
import com.github.simondan.svl.app.R;
import com.google.android.material.snackbar.Snackbar;

import java.util.Objects;

/**
 * @author Simon Danner, 24.11.2019
 */
public final class AndroidUtil
{
  private AndroidUtil()
  {
  }

  public static void showError(View pView, String pMessage)
  {
    Objects.requireNonNull(pView, "The view to show an error for is null!");
    final Snackbar snackbar = Snackbar.make(pView, pMessage, Snackbar.LENGTH_LONG);
    final View snackbarView = snackbar.getView();
    snackbarView.setBackgroundResource(R.color.grey);

    final TextView text = snackbarView.findViewById(com.google.android.material.R.id.snackbar_text);
    text.setBackgroundResource(R.color.grey);
    text.setTextColor(ContextCompat.getColor(pView.getContext(), R.color.errorRed));

    snackbar.show();
  }
}
