package com.caseykulm.oauthheader.services

import com.caseykulm.oauthheader.models.OauthConsumer
import org.junit.Rule
import org.junit.rules.ExternalResource
import org.junit.rules.Verifier

class OauthConsumerSetRule(val oauthConsumer: OauthConsumer) : ExternalResource() {
    override fun before() {
        if (oauthConsumer.consumerKey.startsWith("YOUR_") ||
                oauthConsumer.consumerSecret.startsWith("YOUR_") ||
                oauthConsumer.callbackUrl.startsWith("YOUR_")) {
            TODO("Drop in your own OauthConsumer data")
        }
    }
}
