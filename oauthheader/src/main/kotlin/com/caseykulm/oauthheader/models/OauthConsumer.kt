package com.caseykulm.oauthheader.models

data class OauthConsumer(
    val consumerKey: String,
    val consumerSecret: String,
    val callbackUrl: String = "oob")