package com.caseykulm.oauthheader

import com.caseykulm.oauthheader.models.AccessTokenResponse
import com.caseykulm.oauthheader.models.AuthorizationResponse
import com.caseykulm.oauthheader.models.RequestTokenResponse
import okhttp3.Request

interface Oauth1Api {
    /**
     * Retrieves a Request Token from the OAuth Service, and uses the response
     * from that to form the Authorization URL. This is the URL you send your
     * user to, for them to give your App permissions.
     *
     * NOTE: This is blocking work, and should be performed on a worker thread.
     */
    fun getAuthorizationUrl(): String

    fun parseVerificationResponse(rawQuery: String): AuthorizationResponse

    /**
     * Retrieves an Access Token from the OAuth Service, and returns the token
     * to be stored securely. This is the Token you will sign every request with.
     * Assume that at some point in the future you will receive a 401 unauthorized
     * exception using this access token, in which case you should take them
     * back to the Authorization URL to grant your App permissions again.
     *
     * NOTE: This is blocking work, and should be performed on a worker thread.
     */
    fun getAccessToken(requestTokenResponse: RequestTokenResponse, authorizationResponse: AuthorizationResponse): AccessTokenResponse

    fun getSignedResourceAuthHeader(request: Request, accessToken: String, accessSecret: String): String
}