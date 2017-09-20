package com.caseykulm.oauthheader.header

import com.caseykulm.oauthheader.models.OauthConsumer
import com.caseykulm.oauthheader.models.OauthStage
import okhttp3.Request
import java.security.SecureRandom
import java.util.*

class OauthAuthHeaderGenerator(
    val oauthConsumer: OauthConsumer,
    random: Random = SecureRandom(),
    val calendar: Calendar = Calendar.getInstance()) {
  val nonceGenerator = NonceGenerator(random)
  val signatureGenerator = SignatureGenerator(
      oauthConsumer)

  private fun baseOauthStrBuilder() = StringBuilder("OAuth ")

  /**
   * callback, no token, no secret, no verifier
   */
  fun getRequestTokenAuthHeaderValue(request: Request): String {
    val signatureSnapshotData2 = SignatureSnapshotData(calendar.utcTimeStamp(), nonceGenerator.generate())
    val oauthFieldsSorted = buildOauthFieldsSorted(OauthStage.GET_REQUEST_TOKEN, signatureSnapshotData2)
    val signature = signatureGenerator.getSignature(request, oauthFieldsSorted)
    oauthFieldsSorted.put(OAUTH_SIGNATURE, ESCAPER.escape(signature))
    val oauthFieldsString = oauthTreeMapToString(oauthFieldsSorted)
    return baseOauthStrBuilder().append(oauthFieldsString).toString()
  }

  /**
   * no callback, requestToken, requestSecret, verifier
   */
  fun getAccessTokenAuthHeaderValue(
      request: Request,
      verifier: String,
      requestToken: String,
      requestTokenSecret: String): String {
    val signatureSnapshotData2 = SignatureSnapshotData(calendar.utcTimeStamp(), nonceGenerator.generate())
    val oauthFieldsSorted = buildOauthFieldsSorted(OauthStage.GET_ACCESS_TOKEN, signatureSnapshotData2, requestToken, verifier)
    val signature = signatureGenerator.getSignature(request, oauthFieldsSorted, requestTokenSecret)
    oauthFieldsSorted.put(OAUTH_SIGNATURE, ESCAPER.escape(signature))
    val oauthFieldsString = oauthTreeMapToString(oauthFieldsSorted)
    return baseOauthStrBuilder().append(oauthFieldsString).toString()
  }

  /**
   * no callback, accessToken, accessSecret, no verifier
   */
  fun getResourceAuthHeaderValue(
      request: Request,
      accessToken: String,
      accessTokenSecret: String): String {
    val signatureSnapshotData = SignatureSnapshotData(calendar.utcTimeStamp(), nonceGenerator.generate())
    val oauthFieldsSorted = buildOauthFieldsSorted(OauthStage.GET_RESOURCE, signatureSnapshotData, accessToken)
    val signature = signatureGenerator.getSignature(request, oauthFieldsSorted, accessTokenSecret)
    oauthFieldsSorted.put(OAUTH_SIGNATURE, ESCAPER.escape(signature))
    val oauthFieldsString = oauthTreeMapToString(oauthFieldsSorted)
    return baseOauthStrBuilder().append(oauthFieldsString).toString()
  }

  fun oauthTreeMapToString(treeMap: TreeMap<String, String>): String {
    val stringBuilder = StringBuilder()
    var isFirst = true
    for (entry in treeMap.entries) {
      if (!isFirst) {
        stringBuilder.append(", ")
      }

      // e.g. oauth_version="1.0"
      stringBuilder
          .append(entry.key)
          .append("=")
          .append("\"")
          .append(entry.value)
          .append("\"")

      isFirst = false
    }
    return stringBuilder.toString()
  }

  private fun buildOauthFieldsSorted(
      oauthStage: OauthStage,
      signatureSnapshotData: SignatureSnapshotData,
      token: String = "",
      verifier: String = ""): TreeMap<String, String> {
    val newFields = TreeMap<String, String>()
    newFields.put(OAUTH_CONSUMER_KEY, oauthConsumer.consumerKey)
    newFields.put(OAUTH_NONCE, signatureSnapshotData.nonce)
    newFields.put(OAUTH_SIGNATURE_METHOD, OAUTH_SIGNATURE_METHOD_VALUE)
    newFields.put(OAUTH_TIMESTAMP, signatureSnapshotData.timeStamp.toString())
    newFields.put(OAUTH_VERSION, OAUTH_VERSION_VALUE)

    /**
     * Only used when,
     * RequestToken for fetching AccessTokens and
     * AccessToken for authorizing resource requests.
     *
     * Cannot be used for fetching RequestToken
     * since we have no tokens at that point.
     */
    if (oauthStage == OauthStage.GET_ACCESS_TOKEN || oauthStage == OauthStage.GET_RESOURCE) {
      newFields.put(OAUTH_TOKEN, token)
    }

    /**
     * Only used when fetching RequestToken
     */
    if (oauthStage == OauthStage.GET_REQUEST_TOKEN) {
      newFields.put(OAUTH_CALLBACK, ESCAPER.escape(oauthConsumer.callbackUrl))
    }

    /**
     * Only used when fetching AccessToken
     */
    if (oauthStage == OauthStage.GET_ACCESS_TOKEN) {
      newFields.put(OAUTH_VERIFIER, ESCAPER.escape(verifier))
    }

    return newFields
  }
}