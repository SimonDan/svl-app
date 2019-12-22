package com.github.simondan.svl.app.util;

import android.app.Activity;
import android.content.Context;
import android.view.*;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;
import com.github.simondan.svl.communication.utils.SharedUtils;

import java.util.*;
import java.util.function.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.github.simondan.svl.app.util.AndroidUtil.showError;

/**
 * @author Simon Danner, 21.12.2019
 */
abstract class AbstractFromModel<SERVER_PARAM, FORM_MODEL extends AbstractFromModel<SERVER_PARAM, FORM_MODEL>>
{
  private final Window window;
  protected final IComponentFinder componentFinder;
  protected final Class<SERVER_PARAM> serverParamType;
  protected final Map<EditText, String> fieldDescriptions = new LinkedHashMap<>();
  protected final Map<EditText, Condition> fieldVerifiers = new LinkedHashMap<>();

  AbstractFromModel(Activity pActivity, Class<SERVER_PARAM> pServerParamType)
  {
    window = pActivity.getWindow();
    componentFinder = pActivity::findViewById;
    serverParamType = pServerParamType;
  }

  AbstractFromModel(View pView, Window pWindow, Class<SERVER_PARAM> pServerParamType)
  {
    window = pWindow;
    componentFinder = pView::findViewById;
    serverParamType = pServerParamType;
  }

  protected abstract SERVER_PARAM retrieveServerParamFromForm();

  public FORM_MODEL addButton(int pButtonId, Runnable pAction)
  {
    final Button button = (Button) componentFinder.findById(pButtonId);
    button.setOnClickListener(pView ->
    {
      _closeKeyBoard();
      pAction.run();
    });

    //noinspection unchecked
    return (FORM_MODEL) this;
  }

  public boolean allSatisfied()
  {
    final boolean noFieldNull = fieldDescriptions.keySet().stream()
        .map(pEditText -> pEditText.getText().toString())
        .allMatch(this::_notNullNotEmpty);

    final boolean allConditionsSatisfied = fieldVerifiers.entrySet().stream()
        .allMatch(pEntry -> pEntry.getValue().valueVerifier.test(pEntry.getKey().getText().toString()));

    return noFieldNull && allConditionsSatisfied;
  }

  public void doOrToastUnsatisfied(Consumer<SERVER_PARAM> pActionBasedOnResult)
  {
    if (allSatisfied())
    {
      final SERVER_PARAM serverParamProxy = retrieveServerParamFromForm();
      pActionBasedOnResult.accept(serverParamProxy);
    }
    else
      _toastAllUnsatisfied();
  }

  private void _toastAllUnsatisfied()
  {
    final List<String> unsatisfiedFields = fieldDescriptions.entrySet().stream()
        .filter(pEntry -> !_notNullNotEmpty(pEntry.getKey().getText().toString()))
        .map(Map.Entry::getValue)
        .collect(Collectors.toList());

    final String unsatisfiedFieldsString = String.join(", ", unsatisfiedFields);

    if (!unsatisfiedFields.isEmpty())
    {
      final String should = unsatisfiedFields.size() == 1 ? "darf" : "dürfen";
      showError(window.findViewById(android.R.id.content), unsatisfiedFieldsString + " " + should + " nicht leer sein!");
      return;
    }

    //If every field value is set, check other conditions
    fieldVerifiers.entrySet().stream()
        .filter(pEntry -> !pEntry.getValue().valueVerifier.test(pEntry.getKey().getText().toString()))
        .findFirst()
        .map(pEntry -> fieldDescriptions.get(pEntry.getKey()) + " ist ungültig: " + pEntry.getValue().errorMessage)
        .ifPresent(pErrorMessage -> showError(window.findViewById(android.R.id.content), pErrorMessage));
  }

  private void _closeKeyBoard()
  {
    final InputMethodManager inputManager = (InputMethodManager) window.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
    final View currentFocus = window.getCurrentFocus();

    if (inputManager != null && currentFocus != null)
      inputManager.hideSoftInputFromWindow(currentFocus.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
  }

  private boolean _notNullNotEmpty(String pValue)
  {
    return pValue != null && !pValue.isEmpty();
  }

  abstract class AbstractFieldAddition<VALUE, ADDITION extends AbstractFieldAddition<VALUE, ADDITION>>
  {
    protected final EditText editText;
    protected final String description;
    protected final List<Condition> conditions = new ArrayList<>();

    AbstractFieldAddition(int pEditTextId, String pDescription)
    {
      editText = (EditText) componentFinder.findById(pEditTextId);
      description = pDescription;
    }

    public ADDITION requiresLengthBetween(int pMinLength, int pMaxLength)
    {
      final String errorMessage = "Die Länge muss zwischen " + pMinLength + " und " + pMaxLength + " Zeichen liegen!";
      conditions.add(new Condition(pValue -> pValue.length() >= pMinLength && pValue.length() <= pMaxLength, errorMessage));

      //noinspection unchecked
      return (ADDITION) this;
    }

    public ADDITION requiresExactLength(int pExactLength)
    {
      final String errorMessage = "Die Länge muss genau " + pExactLength + " Zeichen betragen!";
      conditions.add(new Condition(pValue -> pValue.length() == pExactLength, errorMessage));

      //noinspection unchecked
      return (ADDITION) this;
    }

    public ADDITION requiresRegex(Pattern pRegex)
    {
      final String errorMessage = "Format nicht erlaubt!";
      conditions.add(new Condition(pValue -> SharedUtils.validatePattern(pRegex, pValue), errorMessage));

      //noinspection unchecked
      return (ADDITION) this;
    }

    protected void registerAddition(AbstractFieldAddition<VALUE, ?> pAddition)
    {
      fieldDescriptions.put(pAddition.editText, pAddition.description);
      pAddition.conditions.forEach(pCondition -> fieldVerifiers.put(editText, pCondition));
    }
  }

  static class Condition
  {
    private final Predicate<String> valueVerifier;
    private final String errorMessage;

    Condition(Predicate<String> pValueVerifier, String pErrorMessage)
    {
      valueVerifier = pValueVerifier;
      errorMessage = pErrorMessage;
    }
  }

  @FunctionalInterface
  interface IComponentFinder
  {
    View findById(int pId);
  }
}
