package com.github.simondan.svl.app.server;

import java.time.Instant;

/**
 * @author Simon Danner, 01.12.2019
 */
public class LastRestoreData
{
  private final String userMail;
  private final Instant timestamp;

  LastRestoreData(String pUserMail, Instant pTimestamp)
  {
    userMail = pUserMail;
    timestamp = pTimestamp;
  }

  public String getUserMail()
  {
    return userMail;
  }

  public Instant getSendTimestamp()
  {
    return timestamp;
  }
}
