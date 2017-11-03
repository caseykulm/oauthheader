package com.caseykulm.oauthheader

import com.caseykulm.oauthheader.header.OAUTH_TOKEN
import com.caseykulm.oauthheader.header.OauthAuthHeaderGenerator
import com.caseykulm.oauthheader.header.toAccessTokenResponse
import com.caseykulm.oauthheader.header.toRequestTokenResponse
import com.caseykulm.oauthheader.models.*
import okhttp3.HttpUrl
import okhttp3.OkHttpClient

import okhttp3.*

internal val AUTH_HEADER_KEY = "Authorization"

class Oauth1Client(
    private val oauthConsumer: OauthConsumer,
    private val oauthService: OauthService,
    private val okHttpClient: OkHttpClient = OkHttpClient()) {

  // region Public API

  /**
   * Step 1: Retrieve Temporary Request Token Credential
   *
   * NOTE: This is blocking work, and should be performed on a worker thread.
   */
  fun getRequestToken(): RequestTokenResponse {
    val requestTokenBodyString = getTokenResponseBodyString(
        OauthStage.GET_REQUEST_TOKEN,
        getPremadeRequest(oauthService.requestTokenUrl))
    return toRequestTokenResponse(requestTokenBodyString)
  }

  /**
   * Step 2: Retrieve an Authorizaiton URL to send the user to
   *
   * Uses the RequestTokenResponse to form the Authorization URL. This is the URL you send your
   * user to, for them to give your App permissions.
   */
  fun getAuthorizationUrl(requestTokenResponse: RequestTokenResponse): String {
    val authorizationUrl = HttpUrl.parse(oauthService.authorizeUrl)
    if (authorizationUrl == null) throw IllegalStateException("Failed to parse authorize url")
    val authorizationUrlAuthed = authorizationUrl.newBuilder()
        .addQueryParameter(OAUTH_TOKEN, requestTokenResponse.oauthToken)
        .build()
    return authorizationUrlAuthed.toString()
  }

  /**
   * Step 3: Parse the intercepted Authorization Response
   *
   * This will be used as input for the next step.
   */
  fun parseVerificationResponse(rawQuery: String): AuthorizationResponse {
    if (!rawQuery.contains("oauth_token")) {
      throw IllegalStateException("oauth_token cannot be parsed from: " + rawQuery)
    } else if (!rawQuery.contains("oauth_verifier")) {
      throw IllegalStateException("oauth_verifier cannot be parsed from: " + rawQuery)
    }

    val values = rawQuery.split("&")
    val valueMap = HashMap<String, String>()
    values.forEach {
      val split = it.split("=")
      valueMap.put(split[0], split[1])
    }
    val requestToken = valueMap.get("oauth_token")
    val oauthVerifier = valueMap.get("oauth_verifier")
    if (requestToken == null || oauthVerifier == null) {
      throw IllegalStateException("response doesn't contain necessary fields")
    }
    val authorizationResponse = AuthorizationResponse(requestToken, oauthVerifier)
    return authorizationResponse
  }

  /**
   * Step 4: Retrieve Long Lasting Access Token Credential
   *
   * Retrieves an Access Token from the OAuth Service, and returns the token
   * to be stored securely. This is the Token you will sign every request with.
   * Assume that at some point in the future you will receive a 401 unauthorized
   * exception using this access token, in which case you should take them
   * back to the Authorization URL to grant your App permissions again.
   *
   * NOTE: This is blocking work, and should be performed on a worker thread.
   */
  fun getAccessToken(
      requestTokenResponse: RequestTokenResponse,
      authorizationResponse: AuthorizationResponse): AccessTokenResponse {
    val requestTokenBodyString = getTokenResponseBodyString(
        OauthStage.GET_ACCESS_TOKEN,
        getPremadeRequest(oauthService.accessTokenUrl),
        requestTokenResponse.oauthToken,
        requestTokenResponse.oauthTokenSecret,
        authorizationResponse.oauthVerifier)
    return toAccessTokenResponse(requestTokenBodyString)
  }

  // endregion

  // region Internal API

  private fun getAuthHeaderValue(
      oauthStage: OauthStage,
      request: Request,
      token: String = "",
      tokenSecret: String = "",
      verifier: String = ""): String {
    val oauthHeaderGenerator = OauthAuthHeaderGenerator(
        oauthConsumer)
    val authHeaderValue: String
    when (oauthStage) {
      OauthStage.GET_REQUEST_TOKEN -> {
        authHeaderValue = oauthHeaderGenerator.getRequestTokenAuthHeaderValue(request)
      }
      OauthStage.GET_ACCESS_TOKEN -> {
        authHeaderValue = oauthHeaderGenerator.getAccessTokenAuthHeaderValue(request, verifier, token, tokenSecret)
      }
      OauthStage.GET_RESOURCE -> {
        authHeaderValue = oauthHeaderGenerator.getResourceAuthHeaderValue(request, token, tokenSecret)
      }
    }
    return authHeaderValue
  }

  /**
   * Only for RequestToken requests, or AccessToken requests.
   *
   * Resource requests should use the Request object for the
   * actual resource.
   */
  private fun getPremadeRequest(tokenUrl: String): Request {
    val tokenHttpUrl = HttpUrl.parse(tokenUrl)
    val tokenOkRequest = Request.Builder()
        .url(tokenHttpUrl)
        .post(FormBody.Builder().build())
        .build()
    return tokenOkRequest
  }

  private fun getTokenResponseBodyString(
      oauthStage: OauthStage,
      request: Request,
      token: String = "",
      tokenSecret: String = "",
      verifier: String = ""): String {
    val authHeaderValue = getAuthHeaderValue(oauthStage, request, token, tokenSecret, verifier)
    val tokenRequestAuthed = request.newBuilder()
        .header(AUTH_HEADER_KEY, authHeaderValue)
        .build()
    val tokenOkResponse = okHttpClient.newCall(tokenRequestAuthed).execute()
    val tokenResponseBody = tokenOkResponse.body()
    if (tokenResponseBody == null) throw IllegalStateException("Response body is null")
    return tokenResponseBody.string()
  }

  internal fun getSignedResourceAuthHeader(
      request: Request,
      accessTokenResponse: AccessTokenResponse): String {
    return getAuthHeaderValue(
        OauthStage.GET_RESOURCE,
        request,
        accessTokenResponse.oauthToken,
        accessTokenResponse.oauthTokenSecret)
  }

  // endregion
}