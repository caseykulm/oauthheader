package com.caseykulm.oauthheader

import com.caseykulm.oauthheader.models.TokenResponse

interface Oauth1Api {
    /**
     * Retrieves a Request Token from the OAuth Service, and uses the response
     * from that to form the Authorization URL.
     *
     * NOTE: This is blocking work, and should be performed on a worker thread.
     */
    fun getAuthorizationUrl(): String

    /**
     * Retrieves an Access Token from the OAuth Service, and returns the token
     * to be stored securely.
     *
     * NOTE: This is blocking work, and should be performed on a worker thread.
     */
    fun getAccessTokenRequest(oauthToken: String, oauthVerifier: String): TokenResponse
}