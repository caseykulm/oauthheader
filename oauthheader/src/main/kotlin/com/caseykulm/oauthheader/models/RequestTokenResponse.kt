package com.caseykulm.oauthheader.models

data class RequestTokenResponse(
        val oauthToken: String,
        val oauthTokenSecret: String,
        val oauthCallbackConfirmed: Boolean)