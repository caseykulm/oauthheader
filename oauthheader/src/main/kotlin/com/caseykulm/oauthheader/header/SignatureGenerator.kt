package com.caseykulm.oauthheader.header

import com.caseykulm.oauthheader.models.OauthConsumer
import com.caseykulm.oauthheader.models.OauthStage
import com.google.common.net.UrlEscapers
import okhttp3.Request
import okio.Buffer
import okio.ByteString
import java.security.InvalidKeyException
import java.security.NoSuchAlgorithmException
import java.util.*
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

val ESCAPER = UrlEscapers.urlFormParameterEscaper()

class SignatureGenerator(
    val oauthConsumer: OauthConsumer,
    val calendar: Calendar,
    val nonceGenerator: NonceGenerator) {
  internal fun getSignatureSnapshotData(request: Request, oauthStage: OauthStage, token: String = "", tokenSecret: String = "", verifier: String = ""): SignatureSnapshotData {
    val nonce = nonceGenerator.generate()
    val timeStamp = calendar.utcTimeStamp()
    return SignatureSnapshotData(timeStamp, nonce, getSignatureEncoded(nonce, timeStamp, request, oauthStage, token, tokenSecret, verifier))
  }

  internal fun getSignatureEncoded(nonce: String, timeStamp: Long, request: Request, oauthStage: OauthStage, token: String = "", tokenSecret: String = "", verifier: String = ""): String {
    return ESCAPER.escape(getSignature(nonce, timeStamp, request, oauthStage, token, tokenSecret, verifier))
  }

  internal fun getSignature(nonce: String, timeStamp: Long, request: Request, oauthStage: OauthStage, token: String = "", tokenSecret: String = "", verifier: String = ""): String {
    return getSignature(getSigningKey(oauthStage, tokenSecret), getBaseString(nonce, timeStamp, request, oauthStage, token, verifier))
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

  internal fun getBaseString(
      nonce: String,
      timeStamp: Long,
      request: Request,
      oauthStage: OauthStage,
      token: String = "",
      verifier: String = ""): String {
    return "${getVerb(request)}&${getResourcePathEncoded(request)}&${getParamsEncodedString(nonce, timeStamp, request, oauthStage, token, verifier)}"
  }

  internal fun getParamsEncodedString(
      nonce: String,
      timeStamp: Long,
      request: Request,
      oauthStage: OauthStage,
      token: String = "",
      verifier: String = ""): String {
    val paramsEncodedTree = getParamsEncoded(nonce, timeStamp, request, oauthStage, token, verifier)
    return paramsEncodedTreeToString(paramsEncodedTree)
  }

  internal fun getParamsEncoded(
      nonce: String,
      timeStamp: Long,
      request: Request,
      oauthStage: OauthStage,
      token: String = "",
      verifier: String = ""): TreeMap<String, String> {
    return addFormBodyEncoded(
        request, addQueryParamsEncoded(
          request, addOauthParamsEncoded(
              nonce, timeStamp, oauthStage, token, verifier, TreeMap()
            )
          )
        )
  }

  // add in oauth stuff
  private fun addOauthParamsEncoded(
      nonce: String,
      timeStamp: Long,
      oauthStage: OauthStage,
      token: String = "",
      verifier: String = "",
      params: TreeMap<String, String>): TreeMap<String, String> {
    val updatedParams = TreeMap<String, String>()
    updatedParams.putAll(params)
    updatedParams.put(OAUTH_CONSUMER_KEY, oauthConsumer.consumerKey)
    if (oauthStage == OauthStage.GET_ACCESS_TOKEN || oauthStage == OauthStage.GET_RESOURCE) {
      updatedParams.put(OAUTH_TOKEN, token)
    }
    if (oauthStage == OauthStage.GET_ACCESS_TOKEN) {
      updatedParams.put(OAUTH_VERIFIER, verifier)
    }
    updatedParams.put(OAUTH_NONCE, nonce)
    updatedParams.put(OAUTH_TIMESTAMP, timeStamp.toString())
    updatedParams.put(OAUTH_SIGNATURE_METHOD, OAUTH_SIGNATURE_METHOD_VALUE)
    updatedParams.put(OAUTH_VERSION, OAUTH_VERSION_VALUE)
    if (oauthStage == OauthStage.GET_REQUEST_TOKEN) {
      updatedParams.put(OAUTH_CALLBACK, ESCAPER.escape(oauthConsumer.callbackUrl))
    }
    return updatedParams
  }

  // add in query param stuff
  private fun addQueryParamsEncoded(
      resourceRequest: Request,
      params: TreeMap<String, String>): TreeMap<String, String> {
    val updatedParams = TreeMap<String, String>()
    updatedParams.putAll(params)

    val url = resourceRequest.url()
    for (i in 0..url.querySize() - 1) {
      updatedParams.put(ESCAPER.escape(url.queryParameterName(i)),
          ESCAPER.escape(url.queryParameterValue(i)))
    }

    return updatedParams
  }

  // add in form body stuff
  private fun addFormBodyEncoded(
      resourceRequest: Request,
      params: TreeMap<String, String>): TreeMap<String, String> {
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
      if (!first) base.writeUtf8(ESCAPER.escape("&"))
      first = false
      base.writeUtf8(ESCAPER.escape(entry.key))
      base.writeUtf8(ESCAPER.escape("="))
      base.writeUtf8(ESCAPER.escape(entry.value))
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
  internal fun getSigningKey(oauthStage: OauthStage, tokenSecret: String = ""): String {
    val escapedConsumerSecret = ESCAPER.escape(oauthConsumer.consumerSecret)
    val signingAccessSecret: String
    if (oauthStage == OauthStage.GET_ACCESS_TOKEN|| oauthStage == OauthStage.GET_RESOURCE) {
      signingAccessSecret = tokenSecret
    } else {
      signingAccessSecret = ""
    }
    val escapedAccessSigningSecret = ESCAPER.escape(signingAccessSecret)
    return "${escapedConsumerSecret}&${escapedAccessSigningSecret}"
  }

  internal fun getResourcePathEncoded(request: Request): String {
    return ESCAPER.escape(request.urlToPath())
  }

  internal fun getVerb(request: Request): String {
    return request.method()
  }
}

data class SignatureSnapshotData(val timeStamp: Long, val nonce: String, val signatureEncoded: String)