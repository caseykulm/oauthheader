package com.caseykulm.oauthheader

import com.caseykulm.oauthheader.header.SignatureGenerator
import com.caseykulm.oauthheader.models.OauthConsumer
import com.caseykulm.oauthheader.models.OauthService
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request

class Oauth1Client(
        val oauthConsumer: OauthConsumer,
        val oauthService: OauthService,
        val okHttpClient: OkHttpClient): Oauth1Api {
//    lateinit var signatureGenerator: SignatureGenerator

    init {
//        signatureGenerator = SignatureGenerator(
//                oauthConsumer,
//                accessToken,
//                accessSecret,
//                timeStamp,
//                nonce)
    }

    override fun getAuthorizationRequest(): Request {
        val requestTokenUrl = HttpUrl.parse(oauthService.requestTokenUrl)
        val requestTokenRequest = Request.Builder()
                .url(requestTokenUrl)
                .build()

        val authorizationUrl = HttpUrl.parse(oauthService.authorizeUrl)
        val authorizationRequest = Request.Builder()
                .url(authorizationUrl)
                .build()

        return authorizationRequest
    }

    override fun getAccessTokenRequest(oauthToken: String, oauthVerifier: String) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}