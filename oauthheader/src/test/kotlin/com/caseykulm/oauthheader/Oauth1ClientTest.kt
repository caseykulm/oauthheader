package com.caseykulm.oauthheader

import com.caseykulm.oauthheader.models.OauthConsumer
import com.caseykulm.oauthheader.models.OauthService
import okhttp3.OkHttpClient
import org.junit.Before
import org.junit.Test

import org.junit.Assert.*

class Oauth1ClientTest {
    val oauthConsumer = OauthConsumer("YOUR_KEY", "YOUR_SECRET", "YOUR_CALLBACK_URL")
    val oauthService = OauthService(
            "YOUR_REQUEST_TOKEN_URL",
            "YOUR_AUTHORIZE_URL",
            "YOUR_ACCESS_TOKEN_URLR"
    )
    val okhttpClient = OkHttpClient.Builder().build()
    lateinit var oauthClient: Oauth1Api

    @Before
    fun setUp() {
        oauthClient = Oauth1Client(oauthConsumer, oauthService, okhttpClient)
    }

    @Test
    fun getAuthorizationUrl() {
        if (oauthService.requestTokenUrl.startsWith("YOUR_") ||
                oauthService.authorizeUrl.startsWith("YOUR_") ||
                oauthService.accessTokenUrl.startsWith("YOUR_")) {
            return
        }
        print("authorization url: ${oauthClient.getAuthorizationUrl()}")
    }
}