package com.caseykulm.oauthheader.services

import com.caseykulm.oauthheader.models.OauthService

class RavelryOauthService: OauthService {
    override val requestTokenUrl: String
        get() = "https://www.ravelry.com/oauth/request_token"
    override val authorizeUrl: String
        get() = "https://www.ravelry.com/oauth/authorize"
    override val accessTokenUrl: String
        get() = "https://www.ravelry.com/oauth/access_token"
}