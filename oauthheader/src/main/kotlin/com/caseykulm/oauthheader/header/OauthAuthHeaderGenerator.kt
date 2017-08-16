package com.caseykulm.oauthheader.header

import com.caseykulm.oauthheader.models.OauthConsumer
import okhttp3.Request
import java.security.SecureRandom
import java.util.*

class OauthAuthHeaderGenerator(
        val oauthConsumer: OauthConsumer,
        val accessToken: String,
        accessSecret: String?,
        request: Request,
        random: Random = SecureRandom(),
        calendar: Calendar = Calendar.getInstance()) {
    val nonceGenerator = NonceGenerator(random)
    val signatureGenerator = SignatureGenerator(
            oauthConsumer,
            accessToken,
            accessSecret,
            calendar,
            nonceGenerator,
            request)

    companion object {
        val authHeaderKey = "Authorization"
    }

    fun getAuthHeaderValue(): String {
        val signatureSnapshotData = signatureGenerator.getSignatureSnapshotData()
        return StringBuilder("OAuth ")
                .append(OAUTH_CONSUMER_KEY).append("""="${oauthConsumer.consumerKey}", """)
                .append(OAUTH_NONCE).append("""="${signatureSnapshotData.nonce}", """)
                .append(OAUTH_SIGNATURE).append("""="${signatureSnapshotData.signatureEncoded}", """)
                .append(OAUTH_SIGNATURE_METHOD).append("""="${OAUTH_SIGNATURE_METHOD_VALUE}", """)
                .append(OAUTH_TIMESTAMP).append("""="${signatureSnapshotData.timeStamp.toString()}", """)
                .append(OAUTH_ACCESS_TOKEN).append("""="${accessToken}", """)
                .append(OAUTH_VERSION).append("""="${OAUTH_VERSION_VALUE}", """)
                .append(OAUTH_CALLBACK).append("""="${oauthConsumer.callbackUrl}"""")
                .toString()
    }
}