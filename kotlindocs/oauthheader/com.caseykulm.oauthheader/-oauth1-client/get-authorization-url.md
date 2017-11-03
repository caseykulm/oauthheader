[oauthheader](../../index.md) / [com.caseykulm.oauthheader](../index.md) / [Oauth1Client](index.md) / [getAuthorizationUrl](.)

# getAuthorizationUrl

`fun getAuthorizationUrl(requestTokenResponse: `[`RequestTokenResponse`](../../com.caseykulm.oauthheader.models/-request-token-response/index.md)`): `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)

Step 2: Retrieve an Authorizaiton URL to send the user to

Uses the RequestTokenResponse to form the Authorization URL. This is the URL you send your
user to, for them to give your App permissions.

