package com.caseykulm.oauthheader.header

import com.caseykulm.oauthheader.models.TokenResponse
import okhttp3.Request
import java.util.*
import kotlin.collections.HashMap

val OAUTH_CONSUMER_KEY = "oauth_consumer_key"
val OAUTH_ACCESS_TOKEN = "oauth_token"
val OAUTH_TIMESTAMP = "oauth_timestamp"
val OAUTH_NONCE = "oauth_nonce"
val OAUTH_SIGNATURE = "oauth_signature"
val OAUTH_SIGNATURE_METHOD = "oauth_signature_method"
val OAUTH_VERSION = "oauth_version"
val OAUTH_CALLBACK = "oauth_callback"

val OAUTH_SIGNATURE_METHOD_VALUE = "HMAC-SHA1"
val OAUTH_VERSION_VALUE = "1.0"

fun Calendar.utcTimeStamp(): Long {
    timeZone = TimeZone.getTimeZone("UTC")
    return timeInMillis / 1000
}

/**
 * returns the url only up to the path, i.e. no query params
 */
fun Request.urlToPath(): String {
    return url().newBuilder().query(null).build().toString()
}

fun toTokenResponse(responseStr: String): TokenResponse {
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
    val tokenResponse = TokenResponse(requestToken, oauthTokenSecret, oauthCbConfirmed)
    return tokenResponse
}