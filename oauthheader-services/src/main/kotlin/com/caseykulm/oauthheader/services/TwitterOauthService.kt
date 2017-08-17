package com.caseykulm.oauthheader.services

import com.caseykulm.oauthheader.models.OauthService

class TwitterOauthService: OauthService {
    override val requestTokenUrl: String
        get() = "https://api.twitter.com/oauth/request_token"
    override val authorizeUrl: String
        get() = "https://api.twitter.com/oauth/authorize"
    override val accessTokenUrl: String
        get() = "https://api.twitter.com/oauth/access_token"
}