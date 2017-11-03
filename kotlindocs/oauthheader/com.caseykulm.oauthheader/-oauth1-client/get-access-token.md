[oauthheader](../../index.md) / [com.caseykulm.oauthheader](../index.md) / [Oauth1Client](index.md) / [getAccessToken](.)

# getAccessToken

`fun getAccessToken(requestTokenResponse: `[`RequestTokenResponse`](../../com.caseykulm.oauthheader.models/-request-token-response/index.md)`, authorizationResponse: `[`AuthorizationResponse`](../../com.caseykulm.oauthheader.models/-authorization-response/index.md)`): `[`AccessTokenResponse`](../../com.caseykulm.oauthheader.models/-access-token-response/index.md)

Step 4: Retrieve Long Lasting Access Token Credential

Retrieves an Access Token from the OAuth Service, and returns the token
to be stored securely. This is the Token you will sign every request with.
Assume that at some point in the future you will receive a 401 unauthorized
exception using this access token, in which case you should take them
back to the Authorization URL to grant your App permissions again.

NOTE: This is blocking work, and should be performed on a worker thread.

