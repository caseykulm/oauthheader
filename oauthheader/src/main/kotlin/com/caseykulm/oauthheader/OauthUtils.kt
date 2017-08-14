package com.caseykulm.oauthheader

import com.google.common.net.UrlEscapers
import okhttp3.Request
import java.util.*
import okio.ByteString
import java.util.TreeMap
import okio.Buffer
import java.security.InvalidKeyException
import java.security.NoSuchAlgorithmException
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import kotlin.collections.HashMap

val oauthConsumerKey = "oauth_consumer_key"
val oauthAccessToken = "oauth_token"
val oauthTimestamp = "oauth_timestamp"
val oauthNonce = "oauth_nonce"
val oauthSignature = "oauth_signature"
val oauthSignatureMethod = "oauth_signature_method"
val oauthVersion = "oauth_version"
val oauthCallback = "oauth_callback"

val oauthSignatureMethodValue = "HMAC-SHA1"
val oauthVersionValue = "1.0"

val escaper = UrlEscapers.urlFormParameterEscaper()

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

class NonceGenerator(val random: Random) {
    fun generate(): String {
        val nonce = ByteArray(32)
        random.nextBytes(nonce)
        return ByteString.of(*nonce).base64().replace("\\W".toRegex(), "")
    }
}

class SignatureGenerator(
        val consumerKey: String,
        val consumerSecret: String,
        val accessToken: String,
        val accessSecret: String?,
        val timeStamp: Long,
        val nonce: String,
        val resourceRequest: Request) {
    internal fun getSignatureEncoded(): String {
        return escaper.escape(getSignature())
    }

    internal fun getSignature(): String {
        return getSignature(getSigningKey(), getBaseString())
    }

    private fun getSignature(signingKey: String, baseString: String): String {
        val keySpec = SecretKeySpec(signingKey.toByteArray(), "HmacSHA1")
        val mac: Mac
        try {
            mac = Mac.getInstance("HmacSHA1")
            mac.init(keySpec)
        } catch (e: NoSuchAlgorithmException) {
            throw IllegalStateException(e)
        } catch (e: InvalidKeyException) {
            throw IllegalStateException(e)
        }

        val result = mac.doFinal(baseString.toByteArray(Charsets.UTF_8))
        return ByteString.of(*result).base64()
    }

    internal fun getBaseString(): String {
        return "${getVerb()}&${getResourcePathEncoded()}&${getParamsEncoded()}"
    }

    internal fun getParamsEncoded(): String {
        val parameters = TreeMap<String, String>()
        val paramsWithOauthParams = addOauthParamsEncoded(parameters)
        val paramsWithQueryParams = addQueryParamsEncoded(paramsWithOauthParams, resourceRequest)
        val paramsWithFormBodyParams = addFormBodyEncoded(paramsWithQueryParams, resourceRequest)
        return paramsEncodedTreeToString(paramsWithFormBodyParams)
    }

    // add in oauth stuff
    private fun addOauthParamsEncoded(params: TreeMap<String, String>): TreeMap<String, String> {
        val updatedParams = TreeMap<String, String>()
        updatedParams.putAll(params)

        updatedParams.put(oauthConsumerKey, consumerKey)
        updatedParams.put(oauthAccessToken, accessToken)
        updatedParams.put(oauthNonce, nonce)
        updatedParams.put(oauthTimestamp, timeStamp.toString())
        updatedParams.put(oauthSignatureMethod, oauthSignatureMethodValue)
        updatedParams.put(oauthVersion, oauthVersionValue)

        return updatedParams
    }

    // add in query param stuff
    private fun addQueryParamsEncoded(params: TreeMap<String, String>, resourceRequest: Request): TreeMap<String, String> {
        val updatedParams = TreeMap<String, String>()
        updatedParams.putAll(params)

        val url = resourceRequest.url()
        for (i in 0..url.querySize() - 1) {
            updatedParams.put(escaper.escape(url.queryParameterName(i)),
                    escaper.escape(url.queryParameterValue(i)))
        }

        return updatedParams
    }

    // add in form body stuff
    private fun addFormBodyEncoded(params: TreeMap<String, String>, resourceRequest: Request): TreeMap<String, String> {
        val updatedParams = TreeMap<String, String>()
        updatedParams.putAll(params)

        val requestBody = resourceRequest.body()
        if (requestBody == null) return updatedParams
        val body = Buffer()
        requestBody.writeTo(body)

        while (!body.exhausted()) {
            val keyEnd = body.indexOf('='.toByte())
            if (keyEnd.equals(-1L)) throw IllegalStateException("Key with no value: " + body.readUtf8())
            val key = body.readUtf8(keyEnd)
            body.skip(1) // Equals.

            val valueEnd = body.indexOf('&'.toByte())
            val value = if (valueEnd.equals(-1L)) body.readUtf8() else body.readUtf8(valueEnd)
            if (!valueEnd.equals(-1L)) body.skip(1) // Ampersand.

            updatedParams.put(key, value)
        }

        return updatedParams
    }

    // Convert to encoded string
    private fun paramsEncodedTreeToString(params: TreeMap<String, String>): String {
        val base = Buffer()
        var first = true
        for (entry in params.entries) {
            if (!first) base.writeUtf8(escaper.escape("&"))
            first = false
            base.writeUtf8(escaper.escape(entry.key))
            base.writeUtf8(escaper.escape("="))
            base.writeUtf8(escaper.escape(entry.value))
        }
        val formattedParamsEncoded = base.readUtf8()
        if (formattedParamsEncoded == null) throw IllegalStateException("Failed to create string for formatted params")
        return formattedParamsEncoded
    }

    /**
     * Signature Key
     *
     * The OAuth plugin only supports a single signature method: HMAC-SHA1. This uses a HMAC (Hash-based Message Authentication Code), which looks similar to a normal SHA1 hash, but differs significantly. Importantly, it's immune to length extension attacks. It also needs two pieces: a key and the text to hash. The text is the base string created above.
     *
     * The signature key for HMAC-SHA1 is created by taking the client/consumer secret and the token secret, URL-encoding each, then concatenating them with & into a string.
     *
     * This process is always the same, even if you don't have a token yet.
     *
     * For example, if your client secret is abcd and your token secret is 1234, the key is abcd&1234. If your client secret is abcd, and you don't have a token yet, the key is abcd&.
     */
    internal fun getSigningKey(): String {
        val escapedConsumerSecret = escaper.escape(consumerSecret)
        val signingAccessSecret = if (accessSecret == null) "" else accessSecret
        val escapedAccessSigningSecret = escaper.escape(signingAccessSecret)
        return "${escapedConsumerSecret}&${escapedAccessSigningSecret}"
    }

    internal fun getResourcePathEncoded(): String {
        return escaper.escape(resourceRequest.urlToPath())
    }

    internal fun getVerb(): String {
        return resourceRequest.method()
    }
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