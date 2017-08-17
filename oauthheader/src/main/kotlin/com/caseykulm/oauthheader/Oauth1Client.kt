package com.caseykulm.oauthheader

import com.caseykulm.oauthheader.header.OAUTH_ACCESS_TOKEN
import com.caseykulm.oauthheader.header.OauthAuthHeaderGenerator
import com.caseykulm.oauthheader.header.toTokenResponse
import com.caseykulm.oauthheader.models.OauthConsumer
import com.caseykulm.oauthheader.models.OauthService
import com.caseykulm.oauthheader.models.TokenResponse
import okhttp3.HttpUrl
import okhttp3.OkHttpClient

import okhttp3.*

class Oauth1Client(
        val oauthConsumer: OauthConsumer,
        val oauthService: OauthService,
        val okHttpClient: OkHttpClient): Oauth1Api {
    override fun getAuthorizationUrl(): String {
        println("Step 1: Fetching Oauth Request Token")
        val requestTokenUrl = HttpUrl.parse(oauthService.requestTokenUrl)
        val requestTokenOkRequest = Request.Builder()
                .url(requestTokenUrl)
                .post(FormBody.Builder().build())
                .build()
        val oauthHeaderGenerator = OauthAuthHeaderGenerator(
                oauthConsumer,
                "",
                "")
        val requestTokenRequestAuthed = requestTokenOkRequest.newBuilder()
                .header(OauthAuthHeaderGenerator.authHeaderKey, oauthHeaderGenerator.getAuthHeaderValue(requestTokenOkRequest))
                .build()
        val requestTokenOkResponse = okHttpClient.newCall(requestTokenRequestAuthed).execute()
        val requestTokenResponseBody = requestTokenOkResponse.body()
        if (requestTokenResponseBody == null) throw IllegalStateException("Response body is null")
        val requestTokenBodyString = requestTokenResponseBody.string()

        println("Step 2: Parsing Request Token")
        val requestTokenResponse = toTokenResponse(requestTokenBodyString)

        println("Step 3: Formatting Authorization URL")
        val authorizationUrl = HttpUrl.parse(oauthService.authorizeUrl)
        if (authorizationUrl == null) throw IllegalStateException("Failed to parse authorize url")
        val authorizationUrlAuthed = authorizationUrl.newBuilder()
                .addQueryParameter(OAUTH_ACCESS_TOKEN, requestTokenResponse.oauthToken)
                .build()
        return authorizationUrlAuthed.toString()
    }

    override fun getAccessToken(oauthToken: String, oauthVerifier: String): TokenResponse {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getSignedAuthHeader(accessToken: String, accessSecret: String): String {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}