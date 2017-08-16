package com.caseykulm.oauthheader.models

interface OauthService {
    fun getRequestTokenUrl(): String
    fun getAuthorizeUrl(): String
    fun getAccessTokenUrl(): String
}