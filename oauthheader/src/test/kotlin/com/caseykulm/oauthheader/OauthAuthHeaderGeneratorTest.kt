package com.caseykulm.oauthheader

import com.caseykulm.oauthheader.header.OauthAuthHeaderGenerator
import com.caseykulm.oauthheader.models.OauthConsumer
import okhttp3.FormBody
import okhttp3.Request
import okio.ByteString
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import java.util.*

class OauthAuthHeaderGeneratorTest {
  private lateinit var oauthAuthHeaderGenerator: OauthAuthHeaderGenerator
  val accessToken = "370773112-GmHxMAgYyLbNEtIKZeRNFsMKPR9EyMZeS9weJAEb"
  val accessSecret = "LswwdoUaIvS8ltyTt5jkRh4J50vUPVVHtR2YPi5kE"

  @Before
  fun setup() {
    val consumerKey = "xvz1evFS4wEEPTGEFPHBog"
    val consumerSecret = "kAcSOqF21Fu85e7zjz7ZN2U4ZRhfV3WpwPAoE3Z7kBw"

    val calendar = Calendar.getInstance()
    // Friday, October 14, 2011 8:09:18 PM
    calendar.set(2011, 9, 14, 20, 9, 18)
    val random = object : Random() {
      override fun nextBytes(bytes: ByteArray) {
        if (bytes.size != 32) throw AssertionError()
        val hex = ByteString.decodeBase64("kYjzVBB8Y0ZFabxSWbWovY3uYSQ2pTgmZeNu2VS4c+g")
        val nonce = hex!!.toByteArray()
        System.arraycopy(nonce, 0, bytes, 0, nonce.size)
      }
    }

    val callbackUrl = "cburl"

    val oauthConsumer = OauthConsumer(consumerKey, consumerSecret, callbackUrl)

    oauthAuthHeaderGenerator = OauthAuthHeaderGenerator(
        oauthConsumer,
        random,
        calendar)
  }

  @Test
  fun getAuthHeaderTest() {
    val body = FormBody.Builder()
        .add("status", "Hello Ladies + Gentlemen, a signed OAuth request!")
        .build()
    val resourceReq = Request.Builder()
        .url("https://api.twitter.com/1/statuses/update.json?include_entities=true")
        .post(body)
        .build()

    Assert.assertEquals("OAuth "
        + "oauth_consumer_key=\"xvz1evFS4wEEPTGEFPHBog\", "
        + "oauth_nonce=\"kYjzVBB8Y0ZFabxSWbWovY3uYSQ2pTgmZeNu2VS4cg\", "
        + "oauth_signature=\"tnnArxj06cWHq44gCs1OSKk%2FjLY%3D\", "
        + "oauth_signature_method=\"HMAC-SHA1\", "
        + "oauth_timestamp=\"1318622958\", "
        + "oauth_token=\"370773112-GmHxMAgYyLbNEtIKZeRNFsMKPR9EyMZeS9weJAEb\", "
        + "oauth_version=\"1.0\"",
        oauthAuthHeaderGenerator.getResourceAuthHeaderValue(resourceReq, accessToken, accessSecret))
  }
}