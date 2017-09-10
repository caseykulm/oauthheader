# OAuth Header

An OkHttp helper for generating the four bits needed for 3 legged OAuth. 

1. Request Token
2. Authorize
3. Access Token
4. Resource Request Signing oauthheader 

## Steps from scratch

### Step 0: Define your OAuth constants

Define the key and secret generated for your app by the Service. Also 
define the callback URL you would like the Service to send the Request 
Token to.

```kotlin
val oauthConsumer = OauthConsumer(
  "YOUR_CONSUMER_KEY", 
  "YOUR_CONSUMER_SECRET", 
  "YOUR_CONSUMER_CALLBACK")
```

Define the 3 OAuth endpoints from the Service, or use one of the predefined 
OauthService objects from the oauthheader-services artifact.

```kotlin
val oauthService = OauthService(
  "SERVICE_REQUEST_TOKEN_URL", 
  "SERVICE_AUTHORIZE_URL", 
  "SERVICE_ACCESS_TOKEN_URL")
```

Create an Oauth1Api instance with the Oauth1Client class.

```kotlin
val oauthClient = Oauth1Client(
  oauthConsumer, 
  oauthService, 
  okhttpClient)
```

### Step 1: Get a formatted Authorization URL

This will form the url you should send your users to. If you set 
the callback url, the data for the next step will be sent there.

```kotlin
val authorizationUrl: String = oauthClient.getAuthorizationUrl()
```

### Step 2: Intercept Authorization Response

You should intercept the query string from the Oauth page 
calling your callback and it will look something like this

```kotlin
val rawQueryStr = "oauth_token=123abc&oauth_verifier"
```  

Then you should use this helper to parse the response to 
check if it is valid, and to pass as input to the next step.

```kotlin
val authorizationResponse: AuthorizationResponse = oauthClient.parseVerificationResponse(rawResponseStr)
```

### Step 3: Get Access Token

TODO: Where do they get requestTokenResponse in this flow so far. 
It was abstracted away from them, but will be necessary here.

```kotlin
val accessTokenResponse: AccessTokenResponse = oauthClient.getAccessToken(
  requestTokenResponse, authorizationResponse)
```

### Step 4: Getting a Signed Resource Request Header 

Your request for a resource on Service, that will require OAuth, might look 
something like this.

```kotlin
val body = FormBody.Builder()
    .add("status", "Hello Ladies + Gentlemen, a signed OAuth request!")
    .build()
val resourceRequest = Request.Builder()
    .url("https://api.twitter.com/1/statuses/update.json?include_entities=true")
    .post(body)
    .build()
```

Get a signed header from the oauthClient, providing the stuff we've 
acquired so far.

```kotlin
val signedHeaderValue: String = oauthClient.getSignedResourceAuthHeader(
  resourceRequest, authorizationResponse, accessTokenResponse)
```

Add it to your resource request, also using the provided constant for 
the Header Key.

```kotlin
val signedResourceRequest = resourceRequest.newBuilder()
  .addHeader(Oauth1Client.AUTH_HEADER_KEY, signedHeaderValue)
  .build()
```

And now your good to send off an authenticated request 🎉

