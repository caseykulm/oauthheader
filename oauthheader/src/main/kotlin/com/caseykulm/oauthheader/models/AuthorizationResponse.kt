package com.caseykulm.oauthheader.models

data class AuthorizationResponse(
    val oauthToken: String,
    val oauthVerifier: String)