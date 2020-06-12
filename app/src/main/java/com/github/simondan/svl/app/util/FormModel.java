package com.github.simondan.svl.app.util;

import android.app.Activity;
import android.view.*;
import android.widget.EditText;
import de.adito.ojcms.beans.OJBean;
import de.adito.ojcms.beans.literals.fields.IField;

import java.lang.reflect.*;
import java.util.*;
import java.util.function.Function;

/**
 * @author Simon Danner, 23.11.2019
 */
public class FormModel<SERVER_PARAM extends OJBean> extends AbstractFromModel<SERVER_PARAM, FormModel<SERVER_PARAM>>
{
  private final Map<IField<?>, List<EditText>> beanFieldMapping = new LinkedHashMap<>();
  private final Map<IField<?>, Function<String[], ?>> beanFieldValueMapping = new LinkedHashMap<>();

  public static <SERVER_PARAM extends OJBean> FormModel<SERVER_PARAM> createForActivity(Activity pActivity,
                                                                                        Class<SERVER_PARAM> pServerParamType)
  {
    return new FormModel<>(pActivity, pServerParamType);
  }

  public static <SERVER_PARAM extends OJBean> FormModel<SERVER_PARAM> createForView(View pView, Window pWindow,
                                                                                    Class<SERVER_PARAM> pServerParamType)
  {
    return new FormModel<>(pView, pWindow, pServerParamType);
  }

  private FormModel(Activity pActivity, Class<SERVER_PARAM> pServerParamType)
  {
    super(pActivity, pServerParamType);
  }

  private FormModel(View pView, Window pWindow, Class<SERVER_PARAM> pServerParamType)
  {
    super(pView, pWindow, pServerParamType);
  }

  public SingleFieldAdditionString configureStringFieldAddition(int pEditTextId, String pDescription, IField<?> pBeanField)
  {
    return new SingleFieldAdditionString(pEditTextId, pDescription, pBeanField);
  }

  public <VALUE> SingleFieldAddition<VALUE> configureFieldAddition(int pEditTextId, String pDescription, IField<?> pBeanField)
  {
    return new SingleFieldAddition<>(pEditTextId, pDescription, pBeanField);
  }

  @Override
  protected SERVER_PARAM retrieveServerParamFromForm()
  {
    final SERVER_PARAM serverParam = _createEmptyServerParamBean();

    beanFieldMapping.forEach((pBeanField, pEditTexts) ->
    {
      final Object fieldValue = beanFieldValueMapping.get(pBeanField)
          .apply(pEditTexts.stream()
              .map(pEditText -> pEditText.getText().toString())
              .toArray(String[]::new));

      serverParam.setValue((IField) pBeanField, fieldValue);
    });

    return serverParam;
  }

  private SERVER_PARAM _createEmptyServerParamBean()
  {
    try
    {
      final Constructor<SERVER_PARAM> defaultConstructor = serverParamType.getDeclaredConstructor();
      if (!defaultConstructor.isAccessible())
        defaultConstructor.setAccessible(true);
      return defaultConstructor.newInstance();
    }
    catch (IllegalAccessException | InstantiationException | NoSuchMethodException | InvocationTargetException pE)
    {
      throw new RuntimeException("Unable to create server parameter bean!", pE);
    }
  }

  public class SingleFieldAdditionString extends _AbstractFieldAddition<String, SingleFieldAdditionString>
  {
    private SingleFieldAdditionString(int pEditTextId, String pDescription, IField<?> pBeanField)
    {
      super(pEditTextId, pDescription, pBeanField);
    }

    public FormModel<SERVER_PARAM> doAddStringField()
    {
      final IField<?> beanField = doAddSingleAddition(this);
      beanFieldValueMapping.put(beanField, pValues -> pValues[0]);
      return FormModel.this;
    }
  }

  public class SingleFieldAddition<VALUE> extends _AbstractFieldAddition<VALUE, SingleFieldAddition<VALUE>>
  {
    private SingleFieldAddition(int pEditTextId, String pDescription, IField<?> pBeanField)
    {
      super(pEditTextId, pDescription, pBeanField);
    }

    public MultiFieldAddition<VALUE> combineWithField(int pEditTextId, String pDescription)
    {
      final List<_AbstractFieldAddition<VALUE, ?>> priorAdditions = new ArrayList<>();
      priorAdditions.add(this);

      return new MultiFieldAddition<VALUE>(pEditTextId, pDescription, beanField, priorAdditions);
    }

    public FormModel<SERVER_PARAM> doAddField(Function<String, VALUE> pReturnTypeMapper)
    {
      final IField<?> beanField = doAddSingleAddition(this);
      beanFieldValueMapping.put(beanField, pValues -> pReturnTypeMapper);
      return FormModel.this;
    }
  }

  public class MultiFieldAddition<VALUE> extends _AbstractFieldAddition<VALUE, MultiFieldAddition<VALUE>>
  {
    private final List<_AbstractFieldAddition<VALUE, ?>> priorAdditions;

    private MultiFieldAddition(int pEditTextId, String pDescription, IField<?> pBeanField,
                               List<_AbstractFieldAddition<VALUE, ?>> pPriorAdditions)
    {
      super(pEditTextId, pDescription, pBeanField);
      priorAdditions = pPriorAdditions;
    }

    public FormModel<SERVER_PARAM> doAddFields(Function<String[], VALUE> pReturnTypeMapper)
    {
      priorAdditions.forEach(this::doAddSingleAddition);
      final IField<?> beanField = doAddSingleAddition(this);
      beanFieldValueMapping.put(beanField, pReturnTypeMapper);
      return FormModel.this;
    }
  }

  private abstract class _AbstractFieldAddition<VALUE, ADDITION extends _AbstractFieldAddition<VALUE, ADDITION>>
      extends AbstractFieldAddition<VALUE, ADDITION>
  {
    protected final IField<?> beanField;

    private _AbstractFieldAddition(int pEditTextId, String pDescription, IField<?> pBeanField)
    {
      super(pEditTextId, pDescription);
      beanField = pBeanField;
    }

    protected IField<?> doAddSingleAddition(_AbstractFieldAddition<VALUE, ?> pAddition)
    {
      registerAddition(pAddition);
      beanFieldMapping.computeIfAbsent(beanField, m -> new ArrayList<>()).add(pAddition.editText);
      return beanField;
    }
  }
}
