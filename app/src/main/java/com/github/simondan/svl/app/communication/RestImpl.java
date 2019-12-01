package com.github.simondan.svl.app.communication;

import android.content.Context;
import com.github.simondan.svl.app.communication.exceptions.*;
import com.github.simondan.svl.communication.auth.*;

import java.util.Objects;

/**
 * @author Simon Danner, 18.11.2019
 */
public class RestImpl implements IRestInterface
{
  private static final String PATH_AUTH = "authentication";

  private static final String FORM_FIRST_NAME = "firstName";
  private static final String FORM_LAST_NAME = "lastName";
  private static final String FORM_MAIL = "email";
  private static final String FORM_RESTORE_CODE = "restoreCode";

  private final Context context;
  private final ICredentialsStore credentialsStore;

  RestImpl(Context pContext)
  {
    context = Objects.requireNonNull(pContext, "The context must not be null!");
    credentialsStore = ICredentialsStore.createForContext(pContext);
  }

  @Override
  public void registerNewUser(UserName pUserName, String pMail) throws RequestFailedException,
      RequestTimeoutException
  {
    _newAuthCall("register", pUserName, FORM_MAIL, pMail);
  }

  @Override
  public void requestAuthRestoreCode(UserName pUserName, String pMail) throws RequestFailedException,
      RequestTimeoutException
  {
    RestBuilder.buildNoResultCall(context)
        .path(PATH_AUTH + "/requestCode")
        .method(EMethod.PUT)
        .formParam(pBuilder -> pBuilder
            .add(FORM_FIRST_NAME, pUserName.getFirstName())
            .add(FORM_LAST_NAME, pUserName.getLastName())
            .add(FORM_MAIL, pMail))
        .executeCallNoAuthentication();
  }

  @Override
  public void restoreAuthentication(UserName pUserName, String pRestoreCode) throws RequestFailedException,
      RequestTimeoutException
  {
    _newAuthCall("restore", pUserName, FORM_RESTORE_CODE, pRestoreCode);
  }

  @Override
  public String getDummy() throws AuthenticationImpossibleException, RequestTimeoutException, RequestFailedException
  {
    return RestBuilder.buildCall(context, String.class)
        .path("dummy/test")
        .method(EMethod.GET)
        .executeCall();
  }

  private void _newAuthCall(String pAuthPath, UserName pUserName, String pAdditionalFormKey, String pAdditionalFormValue)
      throws RequestTimeoutException, RequestFailedException
  {
    final AuthenticationResponse authenticationResponse = RestBuilder.buildCall(context, AuthenticationResponse.class)
        .path(PATH_AUTH + "/" + pAuthPath)
        .method(EMethod.POST)
        .formParam(pBuilder -> pBuilder
            .add(FORM_FIRST_NAME, pUserName.getFirstName())
            .add(FORM_LAST_NAME, pUserName.getLastName())
            .add(pAdditionalFormKey, pAdditionalFormValue))
        .executeCallNoAuthentication();

    credentialsStore.setUserName(pUserName);
    credentialsStore.saveNewAuthData(authenticationResponse);
  }
}
