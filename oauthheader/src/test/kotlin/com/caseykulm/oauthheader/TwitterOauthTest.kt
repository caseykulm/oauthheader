package com.caseykulm.oauthheader

import com.caseykulm.oauthheader.header.OauthAuthHeaderGenerator
import com.caseykulm.oauthheader.header.toTokenResponse
import com.caseykulm.oauthheader.models.OauthConsumer
import okhttp3.*
import org.junit.Before
import org.junit.Test

class TwitterOauthTest {
    val requestTokenUrl = "https://api.twitter.com/oauth/request_token"
    val authorizeUrl = "https://api.twitter.com/oauth/authorize"
    val accessTokenUrl = "https://api.twitter.com/oauth/access_token"
    lateinit var client: OkHttpClient

    @Before
    fun setUp() {
        client = OkHttpClient()
    }

    @Test
    fun getUserProfile() {
        val request = Request.Builder()
                .url(accessTokenUrl)
                .post(FormBody.Builder().build())
                .build()
        val consumerKey = "YOUR_KEY"
        val consumerSecret = "YOUR_SECRET"
        val callbackUrl = "http://127.0.0.1:8000/"

        if (consumerKey.startsWith("YOUR_") || consumerSecret.startsWith("YOUR_")) {
            print("Add your key and secret to get started")
            return
        }

        val accessToken = ""
        val accessSecret = ""
        val oauthConsumer = OauthConsumer(consumerKey, consumerSecret, callbackUrl)
        val oauthHeaderGenerator = OauthAuthHeaderGenerator(
                oauthConsumer,
                accessToken,
                accessSecret)
        val oauthRequestHeader = Headers.Builder()
                .add(OauthAuthHeaderGenerator.authHeaderKey, oauthHeaderGenerator.getAuthHeaderValue(request))
        val requestTokenRequest = request.newBuilder()
                .url(accessTokenUrl)
                .post(FormBody.Builder().build())
                .headers(oauthRequestHeader.build())
                .build()

        val response = client.newCall(requestTokenRequest).execute()
        val responseBody = response.body()
        if (responseBody == null) throw IllegalStateException("Response body is null")
        val bodyString = responseBody.string()
        println(bodyString)
        val tokenResponse = toTokenResponse(bodyString)
        println(tokenResponse)

        val authorizeUrlBuilder = HttpUrl.parse(authorizeUrl)?.newBuilder()
        if (authorizeUrlBuilder == null) throw IllegalStateException("authorize url is null")
        authorizeUrlBuilder
                .addQueryParameter("oauth_token", tokenResponse.oauthToken)
                .addQueryParameter("oauth_callback", "http://localhost:8000")
        val authorizeUrl = authorizeUrlBuilder.build()
        println(authorizeUrl)
    }
}