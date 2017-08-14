package com.caseykulm.oauthheader

import okio.ByteString
import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.*

class OauthUtilsTest {
    @Test
    fun getOauthTimeStampTest() {
        val calendar = Calendar.getInstance()
        // Friday, October 14, 2011 8:09:18 PM
        calendar.set(2011, 9, 14, 20, 9, 18)
        val oauthTimeStamp = calendar.utcTimeStamp()
        assertEquals("1318622958", oauthTimeStamp.toString())
    }

    @Test
    fun getOauthNonceTest() {
        val random = object : Random() {
            override fun nextBytes(bytes: ByteArray) {
                if (bytes.size != 32) throw AssertionError()
                val hex = ByteString.decodeBase64("kYjzVBB8Y0ZFabxSWbWovY3uYSQ2pTgmZeNu2VS4c+g")
                val nonce = hex!!.toByteArray()
                System.arraycopy(nonce, 0, bytes, 0, nonce.size)
            }
        }
        val oauthNonce = NonceGenerator(random).generate()
        assertEquals("kYjzVBB8Y0ZFabxSWbWovY3uYSQ2pTgmZeNu2VS4cg", oauthNonce)
    }
}