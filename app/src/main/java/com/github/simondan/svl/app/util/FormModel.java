package com.github.simondan.svl.app.util;

import android.app.Activity;
import android.content.Context;
import android.view.*;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;
import com.github.simondan.svl.communication.utils.SharedUtils;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Predicate;
import java.util.regex.Pattern;
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
  private final Map<EditText, _Condition> conditions = new LinkedHashMap<>();

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
    return _addEditText(pEditTextId, pIdentifier, null, null);
  }

  public FormModel addEditText(int pEditTextId, String pIdentifier, Pattern pRegex)
  {
    final String errorMessage = "Muss " + pRegex + " entsprechen!";
    return _addEditText(pEditTextId, pIdentifier, pValue -> SharedUtils.validatePattern(pRegex, pValue), errorMessage);
  }

  public FormModel addEditText(int pEditTextId, String pIdentifier, int pMinLength, int pMaxLength)
  {
    final String errorMessage = "Die Länge muss zwischen " + pMinLength + " und " + pMaxLength + " Zeichen liegen!";
    return _addEditText(pEditTextId, pIdentifier, pValue -> pValue.length() >= pMinLength && pValue.length() <= pMaxLength, errorMessage);
  }

  public FormModel addEditText(int pEditTextId, String pIdentifier, int pExactLength)
  {
    final String errorMessage = "Die Länge muss genau " + pExactLength + " Zeichen betragen!";
    return _addEditText(pEditTextId, pIdentifier, pValue -> pValue.length() == pExactLength, errorMessage);
  }

  private FormModel _addEditText(int pEditTextId, String pIdentifier, @Nullable Predicate<String> pCondition, @Nullable String pErrorMessage)
  {
    final EditText editText = (EditText) componentFinder.findById(pEditTextId);
    identifiers.put(editText, pIdentifier);

    if (pCondition != null)
      conditions.put(editText, new _Condition(pCondition, pErrorMessage));
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
    final boolean noFieldNull = identifiers.keySet().stream()
        .map(pEditText -> pEditText.getText().toString())
        .allMatch(this::_notNullNotEmpty);

    final boolean allConditionsSatisfied = conditions.entrySet().stream()
        .allMatch(pEntry -> pEntry.getValue().valueVerifier.test(pEntry.getKey().getText().toString()));

    return noFieldNull && allConditionsSatisfied;
  }

  public void toastAllUnsatisfied()
  {
    final List<String> unsatisfiedFields = identifiers.entrySet().stream()
        .filter(pEntry -> !_notNullNotEmpty(pEntry.getKey().getText().toString()))
        .map(Map.Entry::getValue)
        .collect(Collectors.toList());

    final String unsatisfiedFieldsString = String.join(", ", unsatisfiedFields);

    if (!unsatisfiedFields.isEmpty())
    {
      final String should = unsatisfiedFields.size() == 1 ? "darf" : "dürfen";
      showErrorToast(window.getContext(), unsatisfiedFieldsString + " " + should + " nicht leer sein!");
      return;
    }

    //If every field value is set, check other conditions
    conditions.entrySet().stream()
        .filter(pEntry -> !pEntry.getValue().valueVerifier.test(pEntry.getKey().getText().toString()))
        .findFirst()
        .map(pEntry -> identifiers.get(pEntry.getKey()) + " ist ungültig: " + pEntry.getValue().errorMessage)
        .ifPresent(pErrorMessage -> showErrorToast(window.getContext(), pErrorMessage));
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

  private static class _Condition
  {
    private final Predicate<String> valueVerifier;
    private final String errorMessage;

    private _Condition(Predicate<String> pValueVerifier, String pErrorMessage)
    {
      valueVerifier = pValueVerifier;
      errorMessage = pErrorMessage;
    }
  }
}
