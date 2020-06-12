package com.github.simondan.svl.app.communication;

import android.content.Context;
import com.github.simondan.svl.app.communication.config.*;
import com.github.simondan.svl.app.communication.exceptions.*;
import de.adito.ojcms.rest.auth.api.AuthenticationRequest;
import okhttp3.*;

import javax.net.ssl.*;
import java.security.SecureRandom;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import static de.adito.ojcms.rest.auth.util.OJGsonSerializer.GSON_INSTANCE;

/**
 * @author Simon Danner, 16.11.2019
 */
class RestBuilder<RESULT>
{
  private static final MediaType TEXT_MEDIA_TYPE = MediaType.parse("text/plain; charset=utf-8");
  private static final MediaType JSON_MEDIA_TYPE = MediaType.parse("application/json; charset=utf-8");

  private final ICredentialsStore credentialsStore;
  private final Class<RESULT> resultType;
  private String path;
  private EMethod method;
  private RequestBody param;
  private TimeoutConfig timeoutConfig = DefaultConfig.TIMEOUT_CONFIG;

  public static <RESULT> RestBuilder<RESULT> buildCall(Context pContext, Class<RESULT> pResultType)
  {
    return new RestBuilder<>(pContext, pResultType);
  }

  public static RestBuilder<Void> buildNoResultCall(Context pContext)
  {
    return new RestBuilder<>(pContext, Void.class);
  }

  private RestBuilder(Context pContext, Class<RESULT> pResultType)
  {
    credentialsStore = ICredentialsStore.createForContext(pContext);
    resultType = pResultType;
  }

  RestBuilder<RESULT> path(String pPath)
  {
    path = pPath;
    return this;
  }

  RestBuilder<RESULT> method(EMethod pMethod)
  {
    method = pMethod;
    return this;
  }

  RestBuilder<RESULT> textParam(String pParam)
  {
    param = RequestBody.create(TEXT_MEDIA_TYPE, pParam);
    return this;
  }

  RestBuilder<RESULT> jsonParam(Object pParam, Class<?> pParamType)
  {
    param = RequestBody.create(JSON_MEDIA_TYPE, GSON_INSTANCE.toJson(pParam, pParamType));
    return this;
  }

  RestBuilder<RESULT> timeout(TimeUnit pTimeUnit, long pValue)
  {
    timeoutConfig = new TimeoutConfig(pTimeUnit, pValue);
    return this;
  }

  RESULT executeCall() throws RequestTimeoutException, AuthenticationImpossibleException, RequestFailedException
  {
    return _createRestCall().callRestInterface();
  }

  RESULT executeCallNoAuthentication() throws RequestTimeoutException, RequestFailedException
  {
    return _createRestCall().callRestInterfaceNoAuth();
  }

  private RestCall<RESULT> _createRestCall()
  {
    return new RestCall<>(timeoutConfig, credentialsStore, new _CallConfig());
  }

  private class _CallConfig implements IRestCallConfig<RESULT>
  {
    _CallConfig()
    {
      Objects.requireNonNull(resultType, "No result type provided!");
      Objects.requireNonNull(path, "No path provided!");
      Objects.requireNonNull(method, "No method provided!");
    }

    @Override
    public Class<RESULT> getResultType()
    {
      return resultType;
    }

    @Override
    public Request.Builder prepareRequest()
    {
      return _initRequest(path);
    }

    @Override
    public Call buildCall(Request.Builder pBuilder)
    {
      final Request request;
      switch (method)
      {
        case GET:
          request = pBuilder.get().build();
          break;
        case POST:
          request = pBuilder.post(param).build();
          break;
        case PUT:
          request = pBuilder.put(param).build();
          break;
        case DELETE:
          request = pBuilder.delete().build();
          break;
        default:
          throw new RuntimeException("Method " + method + " not supported!");
      }

      return _createCall(request);
    }

    @Override
    public Call createAuthenticationCall()
    {
      final AuthenticationRequest authRequest = credentialsStore.buildAuthenticationRequest();
      return _createCall(_initRequest(DefaultConfig.AUTH_PATH)
          .post(RequestBody.create(JSON_MEDIA_TYPE, GSON_INSTANCE.toJson(authRequest, AuthenticationRequest.class)))
          .build());
    }

    private Call _createCall(Request pRequest)
    {
      return createUnsafeOkHttpClientBuilder()
          .callTimeout(timeoutConfig.getTimeout(), timeoutConfig.getTimeUnit())
          .readTimeout(timeoutConfig.getTimeout(), timeoutConfig.getTimeUnit())
          .writeTimeout(timeoutConfig.getTimeout(), timeoutConfig.getTimeUnit())
          .build()
          .newCall(pRequest);
    }

    private Request.Builder _initRequest(String pPath)
    {
      return new Request.Builder()
          .url(DefaultConfig.createServerUrlFromDefaults() + "/" + pPath);
    }
  }

  private static OkHttpClient.Builder createUnsafeOkHttpClientBuilder() //TODO
  {
    try
    {
      final TrustManager[] trustAllCerts = new TrustManager[]{
          new X509TrustManager()
          {
            @Override
            public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType)
            {
            }

            @Override
            public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType)
            {
            }

            @Override
            public java.security.cert.X509Certificate[] getAcceptedIssuers()
            {
              return new java.security.cert.X509Certificate[]{};
            }
          }
      };

      // Install the all-trusting trust manager
      final SSLContext sslContext = SSLContext.getInstance("SSL");
      sslContext.init(null, trustAllCerts, new SecureRandom());
      // Create an ssl socket factory with our all-trusting manager
      final SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();

      return new OkHttpClient.Builder()
          .sslSocketFactory(sslSocketFactory, (X509TrustManager) trustAllCerts[0])
          .hostnameVerifier((hostname, session) -> true);
    }
    catch (Exception e)
    {
      throw new RuntimeException(e);
    }
  }
}
