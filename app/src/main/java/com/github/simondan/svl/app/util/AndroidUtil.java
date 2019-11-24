package com.github.simondan.svl.app.util;

import android.content.Context;
import android.graphics.Color;
import android.widget.*;

/**
 * @author Simon Danner, 24.11.2019
 */
public final class AndroidUtil
{
  private AndroidUtil()
  {
  }

  public static void showErrorToast(Context pContext, String pMessage)
  {
    final Toast toast = Toast.makeText(pContext, pMessage, Toast.LENGTH_LONG);
    final TextView text = toast.getView().findViewById(android.R.id.message);
    text.setTextColor(Color.RED);
    toast.show();
  }
}
