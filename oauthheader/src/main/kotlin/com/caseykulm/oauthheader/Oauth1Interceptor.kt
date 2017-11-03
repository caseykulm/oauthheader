package com.caseykulm.oauthheader

import com.caseykulm.oauthheader.models.AccessTokenResponse
import com.caseykulm.oauthheader.models.AuthorizationResponse
import java.io.IOException
import okhttp3.Interceptor
import okhttp3.Response

class Oauth1Interceptor(
    private val oauth1Client: Oauth1Client,
    private val accessTokenResponse: AccessTokenResponse) : Interceptor {
  @Throws(IOException::class)
  override fun intercept(chain: Interceptor.Chain): Response? {
    val signedResourceAuthHeader = oauth1Client.getSignedResourceAuthHeader(
        chain.request(), accessTokenResponse)
    val signedRequest = chain.request().newBuilder()
        .addHeader(AUTH_HEADER_KEY, signedResourceAuthHeader)
        .build()
    return chain.proceed(signedRequest)
  }
}
