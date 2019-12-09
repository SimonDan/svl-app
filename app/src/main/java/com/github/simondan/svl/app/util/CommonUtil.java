package com.github.simondan.svl.app.util;

import com.github.simondan.svl.communication.auth.*;

/**
 * @author Simon Danner, 07.12.2019
 */
public final class CommonUtil
{
  private CommonUtil()
  {
  }

  public static UserName newUserName(String pFirstName, String pLastName)
  {
    try
    {
      return UserName.of(pFirstName, pLastName);
    }
    catch (BadUserNameException pE)
    {
      throw new RuntimeException(pE);
    }
  }
}
