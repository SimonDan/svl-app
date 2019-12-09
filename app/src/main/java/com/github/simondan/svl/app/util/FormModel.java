package com.github.simondan.svl.app.util;

import android.app.Activity;
import android.content.Context;
import android.view.*;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;
import com.github.simondan.svl.communication.utils.SharedUtils;

import java.lang.reflect.*;
import java.util.*;
import java.util.function.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.github.simondan.svl.app.util.AndroidUtil.showErrorToast;

/**
 * @author Simon Danner, 23.11.2019
 */
public class FormModel<SERVER_PARAM>
{
  private final Window window;
  private final _IComponentFinder componentFinder;
  private final Class<SERVER_PARAM> serverParamType;
  private final _MethodDeterminer methodDeterminer;
  private final Map<EditText, String> fieldDescriptions = new LinkedHashMap<>();
  private final Map<EditText, _Condition> fieldVerifiers = new LinkedHashMap<>();
  private final Map<Method, List<EditText>> serverParamMethodMapping = new LinkedHashMap<>();
  private final Map<Method, Function<String[], ?>> serverParamTypeMappings = new LinkedHashMap<>();

  public static <SERVER_PARAM> FormModel<SERVER_PARAM> createForActivity(Activity pActivity, Class<SERVER_PARAM> pServerParamType)
  {
    return new FormModel<>(pActivity, pServerParamType);
  }

  public static <SERVER_PARAM> FormModel<SERVER_PARAM> createForView(View pView, Window pWindow, Class<SERVER_PARAM> pServerParamType)
  {
    return new FormModel<>(pView, pWindow, pServerParamType);
  }

  private FormModel(Activity pActivity, Class<SERVER_PARAM> pServerParamType)
  {
    window = pActivity.getWindow();
    componentFinder = pActivity::findViewById;
    serverParamType = pServerParamType;
    methodDeterminer = new _MethodDeterminer(pServerParamType);
  }

  private FormModel(View pView, Window pWindow, Class<SERVER_PARAM> pServerParamType)
  {
    window = pWindow;
    componentFinder = pView::findViewById;
    serverParamType = pServerParamType;
    methodDeterminer = new _MethodDeterminer(pServerParamType);
  }

  public SingleFieldAdditionString initStringFieldAddition(int pEditTextId, String pDescription, Function<SERVER_PARAM, String> pMethodMapping)
  {
    return new SingleFieldAdditionString(pEditTextId, pDescription, pMethodMapping);
  }

  public <VALUE> SingleFieldAddition<VALUE> initFieldAddition(int pEditTextId, String pDescription, Function<SERVER_PARAM, VALUE> pMethodMapping)
  {
    return new SingleFieldAddition<>(pEditTextId, pDescription, pMethodMapping);
  }

  public FormModel<SERVER_PARAM> addButton(int pButtonId, Runnable pAction)
  {
    final Button button = (Button) componentFinder.findById(pButtonId);
    button.setOnClickListener(pView ->
    {
      _closeKeyBoard();
      pAction.run();
    });
    return this;
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
      final SERVER_PARAM serverParamProxy = _createServerParamProxy((proxy, method, args) ->
          Optional.ofNullable(serverParamMethodMapping.get(method))
              .map(pFields -> serverParamTypeMappings.get(method)
                  .apply(pFields.stream()
                      .map(pEditText -> pEditText.getText().toString())
                      .toArray(String[]::new)))
              .orElse(null));

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
      showErrorToast(window.getContext(), unsatisfiedFieldsString + " " + should + " nicht leer sein!");
      return;
    }

    //If every field value is set, check other conditions
    fieldVerifiers.entrySet().stream()
        .filter(pEntry -> !pEntry.getValue().valueVerifier.test(pEntry.getKey().getText().toString()))
        .findFirst()
        .map(pEntry -> fieldDescriptions.get(pEntry.getKey()) + " ist ungültig: " + pEntry.getValue().errorMessage)
        .ifPresent(pErrorMessage -> showErrorToast(window.getContext(), pErrorMessage));
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

  private SERVER_PARAM _createServerParamProxy(InvocationHandler pInvocationHandler)
  {
    //noinspection unchecked
    return (SERVER_PARAM) Proxy.newProxyInstance(FormModel.class.getClassLoader(), new Class<?>[]{serverParamType}, pInvocationHandler);
  }

  public class SingleFieldAdditionString extends _AbstractFieldAddition<String, SingleFieldAdditionString>
  {
    private SingleFieldAdditionString(int pEditTextId, String pDescription, Function<SERVER_PARAM, String> pMethodMapping)
    {
      super(pEditTextId, pDescription, pMethodMapping);
    }

    public FormModel<SERVER_PARAM> doAddStringField()
    {
      final Method method = doAddSingleAddition(this);
      serverParamTypeMappings.put(method, pValues -> pValues[0]);
      return FormModel.this;
    }
  }

  public class SingleFieldAddition<VALUE> extends _AbstractFieldAddition<VALUE, SingleFieldAddition<VALUE>>
  {
    private SingleFieldAddition(int pEditTextId, String pDescription, Function<SERVER_PARAM, VALUE> pMethodMapping)
    {
      super(pEditTextId, pDescription, pMethodMapping);
    }

    public MultiFieldAddition<VALUE> combineWithField(int pEditTextId, String pDescription)
    {
      final List<_AbstractFieldAddition<VALUE, ?>> priorAdditions = new ArrayList<>();
      priorAdditions.add(this);

      return new MultiFieldAddition<VALUE>(pEditTextId, pDescription, methodMapping, priorAdditions);
    }

    public FormModel<SERVER_PARAM> doAddField(Function<String, VALUE> pReturnTypeMapper)
    {
      final Method method = doAddSingleAddition(this);
      serverParamTypeMappings.put(method, pValues -> pReturnTypeMapper);
      return FormModel.this;
    }
  }

  public class MultiFieldAddition<VALUE> extends _AbstractFieldAddition<VALUE, MultiFieldAddition<VALUE>>
  {
    private final List<_AbstractFieldAddition<VALUE, ?>> priorAdditions;

    private MultiFieldAddition(int pEditTextId, String pDescription, Function<SERVER_PARAM, VALUE> pMethodMapping,
                               List<_AbstractFieldAddition<VALUE, ?>> pPriorAdditions)
    {
      super(pEditTextId, pDescription, pMethodMapping);
      priorAdditions = pPriorAdditions;
    }

    public FormModel<SERVER_PARAM> doAddFields(Function<String[], VALUE> pReturnTypeMapper)
    {
      priorAdditions.forEach(this::doAddSingleAddition);
      final Method method = doAddSingleAddition(this);
      serverParamTypeMappings.put(method, pReturnTypeMapper);
      return FormModel.this;
    }
  }

  private abstract class _AbstractFieldAddition<VALUE, ADDITION extends _AbstractFieldAddition<VALUE, ADDITION>>
  {
    protected final EditText editText;
    protected final String description;
    protected final Function<SERVER_PARAM, VALUE> methodMapping;
    protected final List<_Condition> conditions = new ArrayList<>();

    private _AbstractFieldAddition(int pEditTextId, String pDescription, Function<SERVER_PARAM, VALUE> pMethodMapping)
    {
      editText = (EditText) componentFinder.findById(pEditTextId);
      description = pDescription;
      methodMapping = pMethodMapping;
    }

    public ADDITION requiresLengthBetween(int pMinLength, int pMaxLength)
    {
      final String errorMessage = "Die Länge muss zwischen " + pMinLength + " und " + pMaxLength + " Zeichen liegen!";
      conditions.add(new _Condition(pValue -> pValue.length() >= pMinLength && pValue.length() <= pMaxLength, errorMessage));

      //noinspection unchecked
      return (ADDITION) this;
    }

    public ADDITION requiresExactLength(int pExactLength)
    {
      final String errorMessage = "Die Länge muss genau " + pExactLength + " Zeichen betragen!";
      conditions.add(new _Condition(pValue -> pValue.length() == pExactLength, errorMessage));

      //noinspection unchecked
      return (ADDITION) this;
    }

    public ADDITION requiresRegex(Pattern pRegex)
    {
      final String errorMessage = "Das Format von " + description + " ist ungültig!";
      conditions.add(new _Condition(pValue -> SharedUtils.validatePattern(pRegex, pValue), errorMessage));

      //noinspection unchecked
      return (ADDITION) this;
    }

    protected Method doAddSingleAddition(_AbstractFieldAddition<VALUE, ?> pAddition)
    {
      fieldDescriptions.put(pAddition.editText, pAddition.description);
      pAddition.conditions.forEach(pCondition -> fieldVerifiers.put(editText, pCondition));

      final Method method = methodDeterminer.determineCalledMethod(pAddition.methodMapping);
      serverParamMethodMapping.computeIfAbsent(method, m -> new ArrayList<>()).add(pAddition.editText);

      return method;
    }
  }

  @FunctionalInterface
  private interface _IComponentFinder
  {
    View findById(int pId);
  }

  private class _MethodDeterminer
  {
    private final SERVER_PARAM proxy;
    private Method calledMethod;

    _MethodDeterminer(Class<SERVER_PARAM> pServerParamType)
    {
      if (!pServerParamType.isInterface())
        throw new IllegalArgumentException("Only interface type server parameters supported!");

      proxy = _createServerParamProxy((proxy, method, args) ->
      {
        calledMethod = method;
        return null;
      });
    }

    Method determineCalledMethod(Function<SERVER_PARAM, ?> pCall)
    {
      pCall.apply(proxy);

      if (calledMethod == null)
        throw new IllegalStateException("No method from server param interface called!");

      final Method result = calledMethod;
      calledMethod = null;
      return result;
    }
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
