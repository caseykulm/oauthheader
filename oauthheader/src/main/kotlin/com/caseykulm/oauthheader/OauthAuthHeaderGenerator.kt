package com.caseykulm.oauthheader

import okhttp3.Request
import java.security.SecureRandom
import java.util.*

class OauthAuthHeaderGenerator(
        val consumerKey: String,
        consumerSecret: String,
        val accessToken: String,
        accessSecret: String?,
        resourceRequest: Request,
        random: Random = SecureRandom(),
        calendar: Calendar = Calendar.getInstance()) {
    val timeStamp: Long = calendar.utcTimeStamp()
    val nonce = NonceGenerator(random).generate()
    val signatureGenerator = SignatureGenerator(
            consumerKey,
            consumerSecret,
            accessToken,
            accessSecret,
            timeStamp,
            nonce,
            resourceRequest)

    companion object {
        val authHeaderKey = "Authorization"
    }

    fun getAuthHeaderValue(): String {
        return "OAuth " +
                OAUTH_CONSUMER_KEY + "=\"" + consumerKey + "\", " +
                OAUTH_NONCE + "=\"" + nonce + "\", " +
                OAUTH_SIGNATURE + "=\"" + signatureGenerator.getSignatureEncoded() + "\", " +
                OAUTH_SIGNATURE_METHOD + "=\"" + OAUTH_SIGNATURE_METHOD_VALUE + "\", " +
                OAUTH_TIMESTAMP + "=\"" + timeStamp.toString() + "\", " +
                OAUTH_ACCESS_TOKEN + "=\"" + accessToken + "\", " +
                OAUTH_VERSION + "=\"" + OAUTH_VERSION_VALUE + "\""
    }
}