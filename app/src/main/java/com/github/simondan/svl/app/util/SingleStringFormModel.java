package com.github.simondan.svl.app.util;

import android.app.Activity;
import android.view.*;

/**
 * @author Simon Danner, 21.12.2019
 */
public class SingleStringFormModel extends AbstractFromModel<String, SingleStringFormModel>
{
  public static SingleStringFormModel createForActivity(Activity pActivity)
  {
    return new SingleStringFormModel(pActivity);
  }

  public static SingleStringFormModel createForView(View pView, Window pWindow)
  {
    return new SingleStringFormModel(pView, pWindow);
  }

  private SingleStringFormModel(Activity pActivity)
  {
    super(pActivity, String.class);
  }

  private SingleStringFormModel(View pView, Window pWindow)
  {
    super(pView, pWindow, String.class);
  }

  public SingleStringFieldAddition configureSingleStringFieldAddition(int pEditTextId, String pDescription)
  {
    if (fieldDescriptions.size() > 0)
      throw new IllegalStateException("Only one field allowed!");

    return new SingleStringFieldAddition(pEditTextId, pDescription);
  }

  @Override
  protected String retrieveServerParamFromForm()
  {
    return fieldDescriptions.keySet().iterator().next().getText().toString();
  }

  public class SingleStringFieldAddition extends AbstractFieldAddition<String, SingleStringFieldAddition>
  {
    private SingleStringFieldAddition(int pEditTextId, String pDescription)
    {
      super(pEditTextId, pDescription);
    }

    public SingleStringFormModel doAddSingleStringField()
    {
      registerAddition(this);
      return SingleStringFormModel.this;
    }
  }
}
