package com.caseykulm.oauthheader

data class TokenResponse(
        val oauthToken: String,
        val oauthTokenSecret: String,
        val oauthCallbackConfirmed: Boolean)