package com.caseykulm.oauthheader.models

data class AccessTokenResponse(
    val oauthToken: String,
    val oauthTokenSecret: String)