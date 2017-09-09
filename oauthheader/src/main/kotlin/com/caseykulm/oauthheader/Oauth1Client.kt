package com.caseykulm.oauthheader

import com.caseykulm.oauthheader.header.OAUTH_TOKEN
import com.caseykulm.oauthheader.header.OauthAuthHeaderGenerator
import com.caseykulm.oauthheader.header.toAccessTokenResponse
import com.caseykulm.oauthheader.header.toRequestTokenResponse
import com.caseykulm.oauthheader.models.*
import okhttp3.HttpUrl
import okhttp3.OkHttpClient

import okhttp3.*

class Oauth1Client(
        val oauthConsumer: OauthConsumer,
        val oauthService: OauthService,
        val okHttpClient: OkHttpClient): Oauth1Api {
    override fun getAuthorizationUrl(): String {
        println("Step 1: Fetching Oauth Request Token")
        val requestTokenBodyString = getTokenBodyString(
                OauthStage.GET_REQUEST_TOKEN,
                oauthService.requestTokenUrl)

        println("Step 2: Parsing Request Token")
        val requestTokenResponse = toRequestTokenResponse(requestTokenBodyString)

        println("Step 3: Formatting Authorization URL")
        val authorizationUrl = HttpUrl.parse(oauthService.authorizeUrl)
        if (authorizationUrl == null) throw IllegalStateException("Failed to parse authorize url")
        val authorizationUrlAuthed = authorizationUrl.newBuilder()
                .addQueryParameter(OAUTH_TOKEN, requestTokenResponse.oauthToken)
                .build()
        return authorizationUrlAuthed.toString()
    }

    override fun parseVerificationResponse(rawQuery: String): AuthorizationResponse {
        if (!rawQuery.contains("oauth_token")) {
            throw IllegalStateException("oauth_token cannot be parsed from: " + rawQuery)
        } else if (!rawQuery.contains("oauth_verifier")) {
            throw IllegalStateException("oauth_verifier cannot be parsed from: " + rawQuery)
        }

        val values = rawQuery.split("&")
        val valueMap = HashMap<String, String>()
        values.forEach {
            val split = it.split("=")
            valueMap.put(split[0], split[1])
        }
        val requestToken = valueMap.get("oauth_token")
        val oauthVerifier = valueMap.get("oauth_verifier")
        if (requestToken == null || oauthVerifier == null) {
            throw IllegalStateException("response doesn't contain necessary fields")
        }
        val authorizationResponse = AuthorizationResponse(requestToken, oauthVerifier)
        return authorizationResponse
    }

    override fun getAccessToken(requestTokenResponse: RequestTokenResponse, authorizationResponse: AuthorizationResponse): AccessTokenResponse {
        println("Step 1: Fetching Oauth Access Token")
        val requestTokenBodyString = getTokenBodyString(
                OauthStage.GET_ACCESS_TOKEN,
                oauthService.accessTokenUrl,
                requestTokenResponse.oauthToken,
                requestTokenResponse.oauthTokenSecret)

        println("Step 2: Parsing Access Token")
        return toAccessTokenResponse(requestTokenBodyString)
    }

    private fun getTokenBodyString(
            oauthStage: OauthStage,
            tokenUrl: String,
            token: String = "",
            tokenSecret: String = "",
            verifier: String = ""): String {
        val tokenHttpUrl = HttpUrl.parse(tokenUrl)
        val tokenOkRequest = Request.Builder()
                .url(tokenHttpUrl)
                .post(FormBody.Builder().build())
                .build()
        val oauthHeaderGenerator = OauthAuthHeaderGenerator(
                oauthConsumer)
        val authHeaderValue: String
        when (oauthStage) {
            OauthStage.GET_REQUEST_TOKEN -> {
                authHeaderValue = oauthHeaderGenerator.getRequestTokenAuthHeaderValue(tokenOkRequest)
            }
            OauthStage.GET_ACCESS_TOKEN -> {
                authHeaderValue = oauthHeaderGenerator.getAccessTokenAuthHeaderValue(tokenOkRequest, verifier, token, tokenSecret)
            }
            OauthStage.GET_RESOURCE -> {
                authHeaderValue = oauthHeaderGenerator.getResourceAuthHeaderValue(tokenOkRequest, token, tokenSecret)
            }
        }
        println("Request Header - ${OauthAuthHeaderGenerator.authHeaderKey}: ${authHeaderValue}")
        val tokenRequestAuthed = tokenOkRequest.newBuilder()
                .header(OauthAuthHeaderGenerator.authHeaderKey, authHeaderValue)
                .build()
        val tokenOkResponse = okHttpClient.newCall(tokenRequestAuthed).execute()
        val tokenResponseBody = tokenOkResponse.body()
        if (tokenResponseBody == null) throw IllegalStateException("Response body is null")
        return tokenResponseBody.string()
    }

    override fun getSignedAuthHeader(accessToken: String, accessSecret: String): String {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}