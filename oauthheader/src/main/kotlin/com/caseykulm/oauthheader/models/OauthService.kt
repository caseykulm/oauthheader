package com.caseykulm.oauthheader.models

interface OauthService {
    val requestTokenUrl: String
    val authorizeUrl: String
    val accessTokenUrl: String
}