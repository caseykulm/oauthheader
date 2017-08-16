package com.caseykulm.oauthheader.models

data class OauthService(
        val requestTokenUrl: String,
        val authorizeUrl: String,
        val accessTokenUrl: String)