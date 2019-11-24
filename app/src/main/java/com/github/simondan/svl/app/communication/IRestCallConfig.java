package com.github.simondan.svl.app.communication;

import okhttp3.*;

/**
 * @author Simon Danner, 09.11.2019
 */
interface IRestCallConfig<RESULT>
{
  Class<RESULT> getResultType();

  Request.Builder prepareRequest();

  Call buildCall(Request.Builder pBuilder);

  default Call justCreateCall()
  {
    return buildCall(prepareRequest());
  }

  Call createAuthenticationCall();
}
