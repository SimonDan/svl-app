package com.github.simondan.svl.app.communication;

import android.content.Context;
import com.github.simondan.svl.app.communication.exceptions.*;
import com.github.simondan.svl.communication.auth.AuthenticationResponse;

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
  public void registerNewUser(String pFirstName, String pLastName, String pMail) throws RequestFailedException,
      RequestTimeoutException
  {
    _newAuthCall("register", pFirstName, pLastName, FORM_MAIL, pMail);
  }

  @Override
  public void requestAuthRestoreCode(String pFirstName, String pLastName, String pMail) throws RequestFailedException,
      RequestTimeoutException
  {
    RestBuilder.buildNoResultCall(context)
        .path(PATH_AUTH + "/requestCode")
        .method(EMethod.PUT)
        .formParam(pBuilder -> pBuilder
            .add(FORM_FIRST_NAME, pFirstName)
            .add(FORM_LAST_NAME, pLastName)
            .add(FORM_MAIL, pMail))
        .executeCallNoAuthentication();
  }

  @Override
  public void restoreAuthentication(String pFirstName, String pLastName, String pRestoreCode) throws RequestFailedException,
      RequestTimeoutException
  {
    _newAuthCall("restore", pFirstName, pLastName, FORM_RESTORE_CODE, pRestoreCode);
  }

  @Override
  public String getDummy() throws AuthenticationImpossibleException, RequestTimeoutException, RequestFailedException
  {
    return RestBuilder.buildCall(context, String.class)
        .path("dummy/test")
        .method(EMethod.GET)
        .executeCall();
  }

  private void _newAuthCall(String pAuthPath, String pFirstName, String pLastName, String pAdditionalFormKey, String pAdditionalFormValue)
      throws RequestTimeoutException, RequestFailedException
  {
    final AuthenticationResponse authenticationResponse = RestBuilder.buildCall(context, AuthenticationResponse.class)
        .path(PATH_AUTH + "/" + pAuthPath)
        .method(EMethod.POST)
        .formParam(pBuilder -> pBuilder
            .add(FORM_FIRST_NAME, pFirstName)
            .add(FORM_LAST_NAME, pLastName)
            .add(pAdditionalFormKey, pAdditionalFormValue))
        .executeCallNoAuthentication();

    credentialsStore.setUserData(pFirstName, pLastName);
    credentialsStore.saveNewAuthData(authenticationResponse);
  }
}
