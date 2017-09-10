package com.caseykulm.oauthheader.header

import com.caseykulm.oauthheader.models.OauthConsumer
import com.caseykulm.oauthheader.models.OauthStage
import okhttp3.Request
import java.security.SecureRandom
import java.util.*

class OauthAuthHeaderGenerator(
    val oauthConsumer: OauthConsumer,
    random: Random = SecureRandom(),
    calendar: Calendar = Calendar.getInstance()) {
  val nonceGenerator = NonceGenerator(random)
  val signatureGenerator = SignatureGenerator(
      oauthConsumer,
      calendar,
      nonceGenerator)

  private fun baseOauthStrBuilder() = StringBuilder("OAuth ")

  /**
   * callback, no token, no secret, no verifier
   */
  fun getRequestTokenAuthHeaderValue(request: Request): String {
    return baseOauthStrBuilder().append(oauthTreeMapToString(
        addOauthCallbackFieldSorted(
            addCommonOauthFieldsSorted(
                request, OauthStage.GET_REQUEST_TOKEN, fields = TreeMap()
            )
        )
    )).toString()
  }

  /**
   * no callback, requestToken, requestSecret, verifier
   */
  fun getAccessTokenAuthHeaderValue(
      request: Request,
      verifier: String,
      requestToken: String,
      requestTokenSecret: String): String {
    return baseOauthStrBuilder().append(oauthTreeMapToString(
        addOauthVerifierFieldSorted(
            verifier, addTokenFieldSorted(
            requestToken, addCommonOauthFieldsSorted(
            request, OauthStage.GET_ACCESS_TOKEN, requestToken, requestTokenSecret, TreeMap()
        )
        )
        )
    )).toString()
  }

  /**
   * no callback, accessToken, accessSecret, no verifier
   */
  fun getResourceAuthHeaderValue(
      request: Request,
      accessToken: String,
      accessTokenSecret: String): String {
    return baseOauthStrBuilder().append(oauthTreeMapToString(
        addTokenFieldSorted(
            accessToken, addCommonOauthFieldsSorted(
            request, OauthStage.GET_RESOURCE, accessToken, accessTokenSecret, TreeMap()
        )
        )
    )).toString()
  }

  fun oauthTreeMapToString(treeMap: TreeMap<String, String>): String {
    val stringBuilder = StringBuilder()
    var isFirst = true
    for (entry in treeMap.entries) {
      if (!isFirst) {
        stringBuilder.append(", ")
      }
      stringBuilder.append(entry.key).append("=").append(entry.value)

      isFirst = false
    }
    return stringBuilder.toString()
  }

  private fun addCommonOauthFieldsSorted(
      request: Request,
      oauthStage: OauthStage,
      token: String = "",
      tokenSecret: String = "",
      fields: TreeMap<String, String>): TreeMap<String, String> {
    val newFields = TreeMap<String, String>()
    newFields.putAll(fields)
    val signatureSnapshotData = signatureGenerator.getSignatureSnapshotData(request, oauthStage, token, tokenSecret)
    newFields.put(OAUTH_CONSUMER_KEY, """"${oauthConsumer.consumerKey}"""")
    newFields.put(OAUTH_NONCE, """"${signatureSnapshotData.nonce}"""")
    newFields.put(OAUTH_SIGNATURE, """"${signatureSnapshotData.signatureEncoded}"""")
    newFields.put(OAUTH_SIGNATURE_METHOD, """"${OAUTH_SIGNATURE_METHOD_VALUE}"""")
    newFields.put(OAUTH_TIMESTAMP, """"${signatureSnapshotData.timeStamp}"""")
    newFields.put(OAUTH_VERSION, """"${OAUTH_VERSION_VALUE}"""")
    return newFields
  }

  /**
   * Only used when,
   * RequestToken for fetching AccessTokens and
   * AccessToken for authorizing resource requests.
   *
   * Cannot be used for fetching RequestToken
   * since we have no tokens at that point.
   */
  private fun addTokenFieldSorted(token: String, fields: TreeMap<String, String>): TreeMap<String, String> {
    val newFields = TreeMap<String, String>()
    newFields.putAll(fields)
    newFields.put(OAUTH_TOKEN, """"${token}"""")
    return newFields
  }

  /**
   * Only used when fetching RequestToken
   */
  private fun addOauthCallbackFieldSorted(fields: TreeMap<String, String>): TreeMap<String, String> {
    val newFields = TreeMap<String, String>()
    newFields.putAll(fields)
    newFields.put(OAUTH_CALLBACK, """"${ESCAPER.escape(oauthConsumer.callbackUrl)}"""")
    return newFields
  }

  /**
   * Only used when fetching AccessToken
   */
  private fun addOauthVerifierFieldSorted(verifier: String, fields: TreeMap<String, String>): TreeMap<String, String> {
    val newFields = TreeMap<String, String>()
    newFields.putAll(fields)
    newFields.put(OAUTH_VERIFIER, """"${ESCAPER.escape(verifier)}"""")
    // TODO: AccessToken request is failing with 401, might be verifier/token/tokenSecret??
    return newFields
  }
}