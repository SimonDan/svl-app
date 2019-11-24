package com.github.simondan.svl.app.util;

import android.app.Activity;
import android.content.Context;
import android.view.*;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;

import java.util.*;
import java.util.stream.Collectors;

import static com.github.simondan.svl.app.util.AndroidUtil.showErrorToast;

/**
 * @author Simon Danner, 23.11.2019
 */
public class FormModel
{
  private final Window window;
  private final _ComponentFinder componentFinder;
  private final Map<EditText, String> identifiers = new LinkedHashMap<>();

  public static FormModel createForActivity(Activity pActivity)
  {
    return new FormModel(pActivity);
  }

  public static FormModel createForView(View pView, Window pWindow)
  {
    return new FormModel(pView, pWindow);
  }

  private FormModel(Activity pActivity)
  {
    window = pActivity.getWindow();
    componentFinder = pActivity::findViewById;
  }

  private FormModel(View pView, Window pWindow)
  {
    window = pWindow;
    componentFinder = pView::findViewById;
  }

  public FormModel addEditText(int pEditTextId, String pIdentifier)
  {
    identifiers.put((EditText) componentFinder.findById(pEditTextId), pIdentifier);
    return this;
  }

  public FormModel addButton(int pButtonId, Runnable pAction)
  {
    final Button button = (Button) componentFinder.findById(pButtonId);
    button.setOnClickListener(pView ->
                              {
                                _closeKeyBoard();
                                pAction.run();
                              });
    return this;
  }

  public String value(int pEditTextId)
  {
    return _retrieve(pEditTextId)
        .filter(this::_notNullNotEmpty)
        .orElseThrow(() -> new RuntimeException("Value for edit text with id " + pEditTextId + " is not satisfied!"));
  }

  public boolean allSatisfied()
  {
    return identifiers.keySet().stream()
        .map(pEditText -> pEditText.getText().toString())
        .allMatch(this::_notNullNotEmpty);
  }

  public void toastAllUnsatisfied()
  {
    final List<String> unsatisfiedFields = identifiers.entrySet().stream()
        .filter(pEntry -> !_notNullNotEmpty(pEntry.getKey().getText().toString()))
        .map(Map.Entry::getValue)
        .collect(Collectors.toList());

    final String unsatisfiedFieldsString = String.join(", ", unsatisfiedFields);

    if (unsatisfiedFields.isEmpty())
      return;

    final Context context = identifiers.keySet().iterator().next().getContext();
    final String should = unsatisfiedFields.size() == 1 ? "darf" : "dürfen";

    showErrorToast(context, unsatisfiedFieldsString + " " + should + " nicht leer sein!");
  }

  private Optional<String> _retrieve(int pEditTextId)
  {
    return identifiers.keySet().stream()
        .filter(pEditText -> pEditText.getId() == pEditTextId)
        .findAny()
        .map(pEditText -> pEditText.getText().toString());
  }

  private boolean _notNullNotEmpty(String pValue)
  {
    return pValue != null && !pValue.isEmpty();
  }

  private void _closeKeyBoard()
  {
    final InputMethodManager inputManager = (InputMethodManager) window.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
    final View currentFocus = window.getCurrentFocus();

    if (inputManager != null && currentFocus != null)
      inputManager.hideSoftInputFromWindow(currentFocus.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
  }

  @FunctionalInterface
  private interface _ComponentFinder
  {
    View findById(int pId);
  }
}
