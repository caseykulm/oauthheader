package com.caseykulm.oauthheader.models

data class TokenResponse(
        val oauthToken: String,
        val oauthTokenSecret: String,
        val oauthCallbackConfirmed: Boolean)