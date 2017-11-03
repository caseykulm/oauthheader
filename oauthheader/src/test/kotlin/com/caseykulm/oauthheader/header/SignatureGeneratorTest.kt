package com.caseykulm.oauthheader.header

import com.caseykulm.oauthheader.models.OauthConsumer
import okhttp3.FormBody
import okhttp3.Request
import okio.ByteString
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.util.*

class SignatureGeneratorTest {
  private lateinit var siggyGen: SignatureGenerator
  val accessToken = "370773112-GmHxMAgYyLbNEtIKZeRNFsMKPR9EyMZeS9weJAEb"
  val accessSecret = "LswwdoUaIvS8ltyTt5jkRh4J50vUPVVHtR2YPi5kE"

  @Before
  fun setUp() {
    siggyGen = getStubSiggyGen()
  }

  @Test
  fun getSigningKeyTest() {
    assertEquals(
        "kAcSOqF21Fu85e7zjz7ZN2U4ZRhfV3WpwPAoE3Z7kBw&LswwdoUaIvS8ltyTt5jkRh4J50vUPVVHtR2YPi5kE",
        siggyGen.getSigningKey(accessSecret))
  }

  @Test
  fun getSignatureBaseString() {
    assertEquals(
        "POST&https%3A%2F%2Fapi.twitter.com%2F1%2Fstatuses%2Fupdate.json&include_entities%3Dtrue%26oauth_consumer_key%3Dxvz1evFS4wEEPTGEFPHBog%26oauth_nonce%3DkYjzVBB8Y0ZFabxSWbWovY3uYSQ2pTgmZeNu2VS4cg%26oauth_signature_method%3DHMAC-SHA1%26oauth_timestamp%3D1318622958%26oauth_token%3D370773112-GmHxMAgYyLbNEtIKZeRNFsMKPR9EyMZeS9weJAEb%26oauth_version%3D1.0%26status%3DHello%2520Ladies%2520%252B%2520Gentlemen%252C%2520a%2520signed%2520OAuth%2520request%2521",
        siggyGen.getBaseString(getResourceRequest(), getStubOauthParams()))
  }

  @Test
  fun getSignatureBaseStringMethodPart() {
    assertEquals("POST", siggyGen.getVerb(getResourceRequest()))
  }

  @Test
  fun getSignatureBaseStringUrlPathPart() {
    assertEquals(
        "https%3A%2F%2Fapi.twitter.com%2F1%2Fstatuses%2Fupdate.json",
        siggyGen.getResourcePathEncoded(getResourceRequest()))
  }

  @Test
  fun getSignatureBaseStringParamsPart() {
    assertEquals(
        "include_entities%3Dtrue%26oauth_consumer_key%3Dxvz1evFS4wEEPTGEFPHBog%26oauth_nonce%3DkYjzVBB8Y0ZFabxSWbWovY3uYSQ2pTgmZeNu2VS4cg%26oauth_signature_method%3DHMAC-SHA1%26oauth_timestamp%3D1318622958%26oauth_token%3D370773112-GmHxMAgYyLbNEtIKZeRNFsMKPR9EyMZeS9weJAEb%26oauth_version%3D1.0%26status%3DHello%2520Ladies%2520%252B%2520Gentlemen%252C%2520a%2520signed%2520OAuth%2520request%2521",
        siggyGen.getParamsEncodedString(
            getResourceRequest(),
            getStubOauthParams()))
  }


  @Test
  fun getSignatureEncoded() {
    assertEquals(
        "tnnArxj06cWHq44gCs1OSKk%2FjLY%3D",
        ESCAPER.escape(siggyGen.getSignature(getResourceRequest(), getStubOauthParams(), accessSecret)))
  }

  private fun getStubOauthParams(): TreeMap<String, String> {
    val oauthParams = TreeMap<String, String>()
    oauthParams.put(OAUTH_CONSUMER_KEY, getStubOauthConsumer().consumerKey)
    oauthParams.put(OAUTH_NONCE, getNonceGenerator().generate())
    oauthParams.put(OAUTH_SIGNATURE_METHOD, OAUTH_SIGNATURE_METHOD_VALUE)
    oauthParams.put(OAUTH_TIMESTAMP, getCalendar().utcTimeStamp().toString())
    oauthParams.put(OAUTH_VERSION, OAUTH_VERSION_VALUE)
    oauthParams.put(OAUTH_TOKEN, accessToken)
    return oauthParams
  }

  private fun getNonceGenerator(): NonceGenerator {
    val random = object : Random() {
      override fun nextBytes(bytes: ByteArray) {
        if (bytes.size != 32) throw AssertionError()
        val hex = ByteString.decodeBase64("kYjzVBB8Y0ZFabxSWbWovY3uYSQ2pTgmZeNu2VS4c+g")
        val nonce = hex!!.toByteArray()
        System.arraycopy(nonce, 0, bytes, 0, nonce.size)
      }
    }
    return NonceGenerator(random)
  }

  private fun getCalendar(): Calendar {
    val calendar = Calendar.getInstance()
    // Friday, October 14, 2011 8:09:18 PM
    calendar.set(2011, 9, 14, 20, 9, 18)
    return calendar
  }

  private fun getResourceRequest(): Request {
    val body = FormBody.Builder()
        .add("status", "Hello Ladies + Gentlemen, a signed OAuth request!")
        .build()
    val resourceReq = Request.Builder()
        .url("https://api.twitter.com/1/statuses/update.json?include_entities=true")
        .post(body)
        .build()
    return resourceReq
  }

  private fun getStubOauthConsumer(): OauthConsumer {
    val consumerKey = "xvz1evFS4wEEPTGEFPHBog"
    val consumerSecret = "kAcSOqF21Fu85e7zjz7ZN2U4ZRhfV3WpwPAoE3Z7kBw"
    val callbackUrl = "http://localhost:8000"
    return OauthConsumer(consumerKey, consumerSecret, callbackUrl)
  }

  private fun getStubSiggyGen(): SignatureGenerator {
    val oauthConsumer = getStubOauthConsumer()
    val siggyGen = SignatureGenerator(
        oauthConsumer)
    return siggyGen
  }
}