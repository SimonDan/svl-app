package com.github.simondan.svl.app.communication.config;

import java.util.concurrent.TimeUnit;

/**
 * @author Simon Danner, 16.11.2019
 */
public final class DefaultConfig
{
  private DefaultConfig()
  {
  }

  public static String SERVER_HOST = "192.168.178.83";
  public static int SERVER_PORT = 8080;
  public static String SERVER_APP_NAME = "svl-server";
  public static TimeoutConfig TIMEOUT_CONFIG = new TimeoutConfig(TimeUnit.SECONDS, 30);

  public static String AUTH_PATH = "authentication/auth";
  public static String AUTH_FORM_FIRST_NAME = "firstName";
  public static String AUTH_FORM_LAST_NAME = "lastName";
  public static String AUTH_FORM_PASSWORD = "password";


  public static String createServerUrlFromDefaults()
  {
    return "https://" + SERVER_HOST + ":" + SERVER_PORT + "/" + SERVER_APP_NAME;
  }
}
