package com.github.simondan.svl.app.communication;

import android.content.Context;
import com.github.simondan.svl.app.communication.config.*;
import com.github.simondan.svl.app.communication.exceptions.*;
import com.google.gson.Gson;
import okhttp3.*;

import javax.net.ssl.*;
import java.security.SecureRandom;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * @author Simon Danner, 16.11.2019
 */
class RestBuilder<RESULT>
{
  private static final Gson GSON = new Gson();
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

  RestBuilder<RESULT> jsonParam(Object pParam)
  {
    param = RequestBody.create(JSON_MEDIA_TYPE, GSON.toJson(pParam));
    return this;
  }

  RestBuilder<RESULT> formParam(Consumer<FormBody.Builder> pBuilderConsumer)
  {
    final FormBody.Builder builder = new FormBody.Builder();
    pBuilderConsumer.accept(builder);
    param = builder.build();
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
      //if (resultType != Void.class)
      //{
      //  Objects.requireNonNull(resultMediaType, "No result media type provided!");
      //  return webTarget.request(resultMediaType);
      //}

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
      return _createCall(_initRequest(DefaultConfig.AUTH_PATH)
          .post(credentialsStore.buildCredentialsForm())
          .build());
    }

    private Call _createCall(Request pRequest)
    {
      return createUnsafeOkHttpClientBuilder()
          //.callTimeout(timeoutConfig.getTimeout(), timeoutConfig.getTimeUnit())
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
