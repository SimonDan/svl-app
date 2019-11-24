package com.github.simondan.svl.app.server;

/**
 * @author Simon Danner, 23.11.2019
 */
interface IRestTaskCallback<RESULT>
{
  void onTaskStart();

  void onTaskEnd();

  void onTimeout();

  void onAuthenticationImpossible();

  void onServerErrorResponse(String pMessage);

  void onResult(RESULT pResult);
}
