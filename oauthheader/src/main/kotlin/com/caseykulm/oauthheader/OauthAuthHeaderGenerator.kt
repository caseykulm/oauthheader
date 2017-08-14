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
                oauthConsumerKey + "=\"" + consumerKey + "\", " +
                oauthNonce + "=\"" + nonce + "\", " +
                oauthSignature + "=\"" + signatureGenerator.getSignatureEncoded() + "\", " +
                oauthSignatureMethod + "=\"" + oauthSignatureMethodValue + "\", " +
                oauthTimestamp + "=\"" + timeStamp.toString() + "\", " +
                oauthAccessToken + "=\"" + accessToken + "\", " +
                oauthVersion + "=\"" + oauthVersionValue + "\""
    }
}