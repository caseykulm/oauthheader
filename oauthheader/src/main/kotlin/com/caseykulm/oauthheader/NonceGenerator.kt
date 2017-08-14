package com.caseykulm.oauthheader

import okio.ByteString
import java.util.*

class NonceGenerator(val random: Random) {
    fun generate(): String {
        val nonce = ByteArray(32)
        random.nextBytes(nonce)
        return ByteString.of(*nonce).base64().replace("\\W".toRegex(), "")
    }
}
