package com.caseykulm.oauthheader

import okhttp3.Request

interface Oauth1Api {
    fun getAuthorizationRequest(): Request
    fun getAccessTokenRequest(oauthToken: String, oauthVerifier: String)
}