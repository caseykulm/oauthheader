package com.caseykulm.oauthheader

import com.caseykulm.oauthheader.models.OauthConsumer
import com.caseykulm.oauthheader.models.OauthService
import okhttp3.Request

class Oauth1Client(
        val oauthConsumer: OauthConsumer,
        val oauthService: OauthService): Oauth1Api {
//    lateinit var signatureGenerator: SignatureGenerator

    init {
//        signatureGenerator = SignatureGenerator(
//                oauthConsumer,
//                accessToken,
//                accessSecret,
//                timeStamp,
//                nonce,
//                resourceRequest)
    }

    override fun getAuthorizationRequest(): Request {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getAccessTokenRequest(oauthToken: String, oauthVerifier: String) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}