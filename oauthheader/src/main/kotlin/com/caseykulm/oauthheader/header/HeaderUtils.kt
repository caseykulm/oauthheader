package com.caseykulm.oauthheader.header

import com.caseykulm.oauthheader.models.AccessTokenResponse
import com.caseykulm.oauthheader.models.RequestTokenResponse
import okhttp3.Request
import java.util.*
import kotlin.collections.HashMap

internal val OAUTH_CONSUMER_KEY = "oauth_consumer_key"
internal val OAUTH_TOKEN = "oauth_token"
internal val OAUTH_TIMESTAMP = "oauth_timestamp"
internal val OAUTH_NONCE = "oauth_nonce"
internal val OAUTH_SIGNATURE = "oauth_signature"
internal val OAUTH_SIGNATURE_METHOD = "oauth_signature_method"
internal val OAUTH_VERSION = "oauth_version"
internal val OAUTH_CALLBACK = "oauth_callback"
internal val OAUTH_VERIFIER = "oauth_verifier"

internal val OAUTH_SIGNATURE_METHOD_VALUE = "HMAC-SHA1"
internal val OAUTH_VERSION_VALUE = "1.0"

internal fun Calendar.utcTimeStamp(): Long {
  timeZone = TimeZone.getTimeZone("UTC")
  return timeInMillis / 1000
}

/**
 * returns the url only up to the path, i.e. no query params
 */
internal fun Request.urlToPath(): String {
  return url().newBuilder().query(null).build().toString()
}

internal fun toRequestTokenResponse(responseStr: String): RequestTokenResponse {
  if (!responseStr.contains("oauth_token")) {
    throw IllegalStateException("oauth_token cannot be parsed from: " + responseStr)
  } else if (!responseStr.contains("oauth_token_secret")) {
    throw IllegalStateException("oauth_token_secret cannot be parsed from: " + responseStr)
  }

  val values = responseStr.split("&")
  val valueMap = HashMap<String, String>()
  values.forEach {
    val split = it.split("=")
    valueMap.put(split[0], split[1])
  }
  val requestToken = valueMap.get("oauth_token")
  val oauthTokenSecret = valueMap.get("oauth_token_secret")
  val oauthCbConfirmed = valueMap.get("oauth_callback_confirmed")?.toBoolean()
  if (requestToken == null || oauthTokenSecret == null || oauthCbConfirmed == null) {
    throw IllegalStateException("response doesn't contain necessary fields")
  }
  val requestTokenResponse = RequestTokenResponse(requestToken, oauthTokenSecret, oauthCbConfirmed)
  return requestTokenResponse
}

internal fun toAccessTokenResponse(responseStr: String): AccessTokenResponse {
  if (!responseStr.contains("oauth_token")) {
    throw IllegalStateException("oauth_token cannot be parsed from: " + responseStr)
  } else if (!responseStr.contains("oauth_token_secret")) {
    throw IllegalStateException("oauth_token_secret cannot be parsed from: " + responseStr)
  }

  val values = responseStr.split("&")
  val valueMap = HashMap<String, String>()
  values.forEach {
    val split = it.split("=")
    valueMap.put(split[0], split[1])
  }
  val accessToken = valueMap.get("oauth_token")
  val accessTokenSecret = valueMap.get("oauth_token_secret")
  if (accessToken == null || accessTokenSecret == null) {
    throw IllegalStateException("response doesn't contain necessary fields")
  }
  val accessTokenResponse = AccessTokenResponse(accessToken, accessTokenSecret)
  return accessTokenResponse
}