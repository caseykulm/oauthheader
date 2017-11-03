[oauthheader](../../index.md) / [com.caseykulm.oauthheader](../index.md) / [Oauth1Client](.)

# Oauth1Client

`class Oauth1Client`

### Constructors

| Name | Summary |
|---|---|
| [&lt;init&gt;](-init-.md) | `Oauth1Client(oauthConsumer: `[`OauthConsumer`](../../com.caseykulm.oauthheader.models/-oauth-consumer/index.md)`, oauthService: `[`OauthService`](../../com.caseykulm.oauthheader.models/-oauth-service/index.md)`, okHttpClient: OkHttpClient)` |

### Functions

| Name | Summary |
|---|---|
| [getAccessToken](get-access-token.md) | `fun getAccessToken(requestTokenResponse: `[`RequestTokenResponse`](../../com.caseykulm.oauthheader.models/-request-token-response/index.md)`, authorizationResponse: `[`AuthorizationResponse`](../../com.caseykulm.oauthheader.models/-authorization-response/index.md)`): `[`AccessTokenResponse`](../../com.caseykulm.oauthheader.models/-access-token-response/index.md)<br>Step 4: Retrieve Long Lasting Access Token Credential |
| [getAuthorizationUrl](get-authorization-url.md) | `fun getAuthorizationUrl(requestTokenResponse: `[`RequestTokenResponse`](../../com.caseykulm.oauthheader.models/-request-token-response/index.md)`): `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)<br>Step 2: Retrieve an Authorizaiton URL to send the user to |
| [getRequestToken](get-request-token.md) | `fun getRequestToken(): `[`RequestTokenResponse`](../../com.caseykulm.oauthheader.models/-request-token-response/index.md)<br>Step 1: Retrieve Temporary Request Token Credential |
| [parseVerificationResponse](parse-verification-response.md) | `fun parseVerificationResponse(rawQuery: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`): `[`AuthorizationResponse`](../../com.caseykulm.oauthheader.models/-authorization-response/index.md)<br>Step 3: Parse the intercepted Authorization Response |
