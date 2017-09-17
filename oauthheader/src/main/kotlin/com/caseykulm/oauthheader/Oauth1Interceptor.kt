package com.caseykulm.oauthheader

import com.caseykulm.oauthheader.models.AccessTokenResponse
import com.caseykulm.oauthheader.models.AuthorizationResponse
import java.io.IOException
import okhttp3.Interceptor
import okhttp3.Response

class Oauth1Interceptor(
    val oauth1Client: Oauth1Client,
    val accessTokenResponse: AccessTokenResponse) : Interceptor {
  @Throws(IOException::class)
  override fun intercept(chain: Interceptor.Chain): Response? {
    val signedResourceAuthHeader = oauth1Client.getSignedResourceAuthHeader(
        chain.request(), accessTokenResponse)
    val signedRequest = chain.request().newBuilder()
        .addHeader(Oauth1Client.AUTH_HEADER_KEY, signedResourceAuthHeader)
        .build()
    return chain.proceed(signedRequest)
  }
}
