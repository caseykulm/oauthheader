package com.caseykulm.oauthheader

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
                .url(requestTokenUrl)
                .post(FormBody.Builder().build())
                .build()
        val consumerKey = "YOUR_CONSUMER_KEY"
        val consumerSecret = "YOUR_CONSUMER_SECRET"

        if (consumerKey.startsWith("YOUR_") || consumerSecret.startsWith("YOUR_")) {
            print("Add your key and secret to get started")
            return
        }

        val accessToken = ""
        val accessSecret = ""
        val oauthHeaderGenerator = OauthAuthHeaderGenerator(
                consumerKey,
                consumerSecret,
                accessToken,
                accessSecret,
                request)
        val oauthRequestHeader = Headers.Builder()
                .add(OauthAuthHeaderGenerator.authHeaderKey, oauthHeaderGenerator.getAuthHeaderValue())
        val requestTokenRequest = request.newBuilder()
                .headers(oauthRequestHeader.build())
                .build()

        val response = client.newCall(requestTokenRequest).execute()
        val responseBody = response.body()
        if (responseBody == null) throw IllegalStateException("Response body is null")
        val bodyString = responseBody.string()
        val tokenResponse = toTokenResponse(bodyString)
        println(tokenResponse)

        val authorizeUrlBuilder = HttpUrl.parse(authorizeUrl)?.newBuilder()
        if (authorizeUrlBuilder == null) throw IllegalStateException("authorize url is null")
        authorizeUrlBuilder
                .addQueryParameter("oauth_token", tokenResponse.oauthToken)
                .addQueryParameter("oauth_callback", "oob")
        val authorizeUrl = authorizeUrlBuilder.build()
        println(authorizeUrl)
    }
}