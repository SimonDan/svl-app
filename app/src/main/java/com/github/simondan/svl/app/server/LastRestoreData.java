package com.github.simondan.svl.app.server;

import com.github.simondan.svl.communication.auth.UserName;

import java.time.Instant;

/**
 * @author Simon Danner, 01.12.2019
 */
public class LastRestoreData
{
  private final UserName userName;
  private final Instant timestamp;

  LastRestoreData(UserName pUserName, Instant pTimestamp)
  {
    userName = pUserName;
    timestamp = pTimestamp;
  }

  public UserName getUserName()
  {
    return userName;
  }

  public Instant getSendTimestamp()
  {
    return timestamp;
  }
}
