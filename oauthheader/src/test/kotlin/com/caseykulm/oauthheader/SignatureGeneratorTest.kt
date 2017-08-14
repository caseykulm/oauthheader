package com.caseykulm.oauthheader

import okhttp3.FormBody
import okhttp3.Request
import okio.ByteString
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.util.*

class SignatureGeneratorTest {
    lateinit var siggyGen: SignatureGenerator

    @Before
    fun setUp() {
        siggyGen = getStubSiggyGen()
    }

    @Test
    fun getSigningKeyTest() {
        assertEquals(
                "kAcSOqF21Fu85e7zjz7ZN2U4ZRhfV3WpwPAoE3Z7kBw&LswwdoUaIvS8ltyTt5jkRh4J50vUPVVHtR2YPi5kE",
                siggyGen.getSigningKey())
    }

    @Test
    fun getSignatureBaseString() {
        assertEquals(
                "POST&https%3A%2F%2Fapi.twitter.com%2F1%2Fstatuses%2Fupdate.json&include_entities%3Dtrue%26oauth_consumer_key%3Dxvz1evFS4wEEPTGEFPHBog%26oauth_nonce%3DkYjzVBB8Y0ZFabxSWbWovY3uYSQ2pTgmZeNu2VS4cg%26oauth_signature_method%3DHMAC-SHA1%26oauth_timestamp%3D1318622958%26oauth_token%3D370773112-GmHxMAgYyLbNEtIKZeRNFsMKPR9EyMZeS9weJAEb%26oauth_version%3D1.0%26status%3DHello%2520Ladies%2520%252B%2520Gentlemen%252C%2520a%2520signed%2520OAuth%2520request%2521",
                siggyGen.getBaseString())
    }

    @Test
    fun getSignatureBaseStringMethodPart() {
        assertEquals("POST", siggyGen.getVerb())
    }

    @Test
    fun getSignatureBaseStringUrlPathPart() {
        assertEquals(
                "https%3A%2F%2Fapi.twitter.com%2F1%2Fstatuses%2Fupdate.json",
                siggyGen.getResourcePathEncoded())
    }

    @Test
    fun getSignatureBaseStringParamsPart() {
        assertEquals(
                "include_entities%3Dtrue%26oauth_consumer_key%3Dxvz1evFS4wEEPTGEFPHBog%26oauth_nonce%3DkYjzVBB8Y0ZFabxSWbWovY3uYSQ2pTgmZeNu2VS4cg%26oauth_signature_method%3DHMAC-SHA1%26oauth_timestamp%3D1318622958%26oauth_token%3D370773112-GmHxMAgYyLbNEtIKZeRNFsMKPR9EyMZeS9weJAEb%26oauth_version%3D1.0%26status%3DHello%2520Ladies%2520%252B%2520Gentlemen%252C%2520a%2520signed%2520OAuth%2520request%2521",
                siggyGen.getParamsEncoded())
    }


    @Test
    fun getSiggy() {
        assertEquals("tnnArxj06cWHq44gCs1OSKk%2FjLY%3D", siggyGen.getSignatureEncoded())
    }

    private fun getStubSiggyGen(): SignatureGenerator {
        val calendar = Calendar.getInstance()
        // Friday, October 14, 2011 8:09:18 PM
        calendar.set(2011, 9, 14, 20, 9, 18)
        val oauthTimeStamp = calendar.utcTimeStamp()
        val random = object : Random() {
            override fun nextBytes(bytes: ByteArray) {
                if (bytes.size != 32) throw AssertionError()
                val hex = ByteString.decodeBase64("kYjzVBB8Y0ZFabxSWbWovY3uYSQ2pTgmZeNu2VS4c+g")
                val nonce = hex!!.toByteArray()
                System.arraycopy(nonce, 0, bytes, 0, nonce.size)
            }
        }
        val oauthNonce = NonceGenerator(random).generate()

        val consumerKey = "xvz1evFS4wEEPTGEFPHBog"
        val consumerSecret = "kAcSOqF21Fu85e7zjz7ZN2U4ZRhfV3WpwPAoE3Z7kBw"
        val accessToken = "370773112-GmHxMAgYyLbNEtIKZeRNFsMKPR9EyMZeS9weJAEb"
        val accessSecret = "LswwdoUaIvS8ltyTt5jkRh4J50vUPVVHtR2YPi5kE"

        val body = FormBody.Builder()
                .add("status", "Hello Ladies + Gentlemen, a signed OAuth request!")
                .build()
        val resourceReq = Request.Builder()
                .url("https://api.twitter.com/1/statuses/update.json?include_entities=true")
                .post(body)
                .build()

        val siggyGen = SignatureGenerator(consumerKey, consumerSecret, accessToken, accessSecret, oauthTimeStamp, oauthNonce, resourceReq)
        return siggyGen
    }
}