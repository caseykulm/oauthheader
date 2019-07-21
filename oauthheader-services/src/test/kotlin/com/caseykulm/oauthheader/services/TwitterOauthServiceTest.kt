package com.caseykulm.oauthheader.services

import com.caseykulm.oauthheader.Oauth1Client
import com.caseykulm.oauthheader.models.OauthConsumer
import okhttp3.OkHttpClient
import org.junit.Before
import org.junit.Test

class TwitterOauthServiceTest {
  private val oauthConsumer = OauthConsumer(
      "YOUR_TWITTER_CONSUMER_KEY",
      "YOUR_TWITTER_CONSUMER_SECRET",
      "YOUR_CALLBACK"
  )
  private val oauthService = TwitterOauthService()
  private val okhttpClient = OkHttpClient.Builder().build()
  private lateinit var oauthClient: Oauth1Client

  @Before
  fun setUp() {
    oauthClient = Oauth1Client(oauthConsumer, oauthService, okhttpClient)
  }

  @Test
  fun getAuthorizationUrl() {
    // Step 1: Update oauthconsumer above with your info
    // Step 2: uncomment print line below
    // Step 3: run for instructions
    // print("authorization url: ${oauthClient.getAuthorizationUrl()}")
  }
}