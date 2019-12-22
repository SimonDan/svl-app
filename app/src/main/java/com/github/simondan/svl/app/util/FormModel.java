package com.github.simondan.svl.app.util;

import android.app.Activity;
import android.view.*;
import android.widget.EditText;

import java.lang.reflect.*;
import java.util.*;
import java.util.function.Function;

/**
 * @author Simon Danner, 23.11.2019
 */
public class FormModel<SERVER_PARAM> extends AbstractFromModel<SERVER_PARAM, FormModel<SERVER_PARAM>>
{
  private final _MethodDeterminer methodDeterminer;
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
    super(pActivity, pServerParamType);
    methodDeterminer = new _MethodDeterminer(pServerParamType);
  }

  private FormModel(View pView, Window pWindow, Class<SERVER_PARAM> pServerParamType)
  {
    super(pView, pWindow, pServerParamType);
    methodDeterminer = new _MethodDeterminer(pServerParamType);
  }

  public SingleFieldAdditionString configureStringFieldAddition(int pEditTextId, String pDescription, Function<SERVER_PARAM, String> pMethodMapping)
  {
    return new SingleFieldAdditionString(pEditTextId, pDescription, pMethodMapping);
  }

  public <VALUE> SingleFieldAddition<VALUE> configureFieldAddition(int pEditTextId, String pDescription, Function<SERVER_PARAM, VALUE> pMethodMapping)
  {
    return new SingleFieldAddition<>(pEditTextId, pDescription, pMethodMapping);
  }

  @Override
  protected SERVER_PARAM retrieveServerParamFromForm()
  {
    return _createServerParamProxy((proxy, method, args) ->
        Optional.ofNullable(serverParamMethodMapping.get(method))
            .map(pFields -> serverParamTypeMappings.get(method)
                .apply(pFields.stream()
                    .map(pEditText -> pEditText.getText().toString())
                    .toArray(String[]::new)))
            .orElse(null));
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
      extends AbstractFieldAddition<VALUE, ADDITION>
  {
    protected final Function<SERVER_PARAM, VALUE> methodMapping;

    private _AbstractFieldAddition(int pEditTextId, String pDescription, Function<SERVER_PARAM, VALUE> pMethodMapping)
    {
      super(pEditTextId, pDescription);
      methodMapping = pMethodMapping;
    }

    protected Method doAddSingleAddition(_AbstractFieldAddition<VALUE, ?> pAddition)
    {
      registerAddition(pAddition);

      final Method method = methodDeterminer.determineCalledMethod(pAddition.methodMapping);
      serverParamMethodMapping.computeIfAbsent(method, m -> new ArrayList<>()).add(pAddition.editText);

      return method;
    }
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
}
