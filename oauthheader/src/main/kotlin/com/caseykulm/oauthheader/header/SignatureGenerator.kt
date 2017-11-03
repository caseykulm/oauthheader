package com.caseykulm.oauthheader.header

import com.caseykulm.oauthheader.models.OauthConsumer
import com.google.common.net.UrlEscapers
import okhttp3.Request
import okio.Buffer
import okio.ByteString
import java.security.InvalidKeyException
import java.security.NoSuchAlgorithmException
import java.util.*
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

internal val ESCAPER = UrlEscapers.urlFormParameterEscaper()

internal class SignatureGenerator(
    private val oauthConsumer: OauthConsumer) {
  internal fun getSignature(request: Request, oauthParams: TreeMap<String, String>, tokenSecret: String = ""): String {
    return getSignature(getSigningKey(tokenSecret), getBaseString(request, oauthParams))
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
      request: Request,
      oauthParams: TreeMap<String, String>): String {
    return "${getVerb(request)}&${getResourcePathEncoded(request)}&${getParamsEncodedString(request, oauthParams)}"
  }

  internal fun getResourcePathEncoded(request: Request): String {
    return ESCAPER.escape(request.urlToPath())
  }

  internal fun getVerb(request: Request): String {
    return request.method()
  }

  internal fun getParamsEncodedString(
      request: Request,
      oauthParams: TreeMap<String, String>): String {
    val paramsEncodedTree = getParamsEncoded(request, oauthParams)
    return paramsEncodedTreeToString(paramsEncodedTree)
  }

  internal fun getParamsEncoded(
      request: Request,
      oauthParams: TreeMap<String, String>): TreeMap<String, String> {
    return addFormBodyEncoded(request, addQueryParamsEncoded(request, oauthParams))
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
   * Produces a signing key which will be combined with the base string to
   * produce the final signature.
   *
   * This process is always the same, even if you don't have a token yet.
   *
   * For example, if your clientSecret is "abcd",
   *
   * Case 1: Building requestToken signature. Then signing key is "abcd&"
   *
   * Case 2: Building accessToken signature, and requestTokenSecret was foo.
   * Then signing key is "abcd&foo"
   *
   * Case 3: Building resourceToken signature, and accessTokenSecret was bar.
   *  Then signing key is "abcd&bar"
   *
   * Input: tokenSecret from previous step if available, empty string otherwise
   *
   * Output: signing key
   */
  internal fun getSigningKey(tokenSecret: String = ""): String {
    val escapedConsumerSecret = ESCAPER.escape(oauthConsumer.consumerSecret)
    val escapedAccessSigningSecret = ESCAPER.escape(tokenSecret)
    return "${escapedConsumerSecret}&${escapedAccessSigningSecret}"
  }
}
