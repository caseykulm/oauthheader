package com.caseykulm.oauthheader.services

import com.caseykulm.oauthheader.Oauth1Api
import com.caseykulm.oauthheader.Oauth1Client
import com.caseykulm.oauthheader.models.OauthConsumer
import okhttp3.OkHttpClient
import org.junit.Before
import org.junit.Test

class TwitterOauthServiceTest {
    val oauthConsumer = OauthConsumer(
            "YOUR_TWITTER_CONSUMER_KEY",
            "YOUR_TWITTER_CONSUMER_SECRET",
            "YOUR_CALLBACK")
    val oauthService = TwitterOauthService()
    val okhttpClient = OkHttpClient.Builder().build()
    lateinit var oauthClient: Oauth1Api

    @Before
    fun setUp() {
        oauthClient = Oauth1Client(oauthConsumer, oauthService, okhttpClient)
    }

    @Test
    fun getAuthorizationUrl() {
        if (oauthConsumer.consumerKey.startsWith("YOUR_") ||
                oauthConsumer.consumerSecret.startsWith("YOUR_") ||
                oauthConsumer.callbackUrl.startsWith("YOUR_")) {
            print("Drop in your own OauthConsumer data")
            return
        }
        print("authorization url: ${oauthClient.getAuthorizationUrl()}")
    }
}