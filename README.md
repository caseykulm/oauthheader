# OAuth Header

An OkHttp helper for generating the three bits needed for 3 legged OAuth. 

1. Request Token
2. Authorize
3. Access Token

## Example

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

```kotlin
val authorizationUrl = oauthClient.getAuthorizationUrl()
```

### Step 3: Getting Request Token

Create an AccessTokenResponse object.

```kotlin
val accessTokenResponse = AccessTokenResponse("CONSUMER_ACCESS_TOKEN", "CONSUMER_VERIFIER")
```

Your request for a resource on Service, that will require OAuth.

```kotlin
val body = FormBody.Builder()
    .add("status", "Hello Ladies + Gentlemen, a signed OAuth request!")
    .build()
val resourceRequest = Request.Builder()
    .url("https://api.twitter.com/1/statuses/update.json?include_entities=true")
    .post(body)
    .build()
```

Create the ```OauthAuthHeaderGenerator``` object which will create 
the header for us.

```kotlin
val oauthHeaderGenerator = OauthAuthHeaderGenerator(
    consumerKey,
    consumerSecret,
    accessToken,
    accessSecret,
    resourceRequest)
```

Create the authorization header, and add it to to the request.

```kotlin
val oauthRequestHeader = Headers.Builder()
    .add(OauthAuthHeaderGenerator.authHeaderKey, oauthHeaderGenerator.getAuthHeaderValue())
val requestTokenRequest = Request.Builder()
    .headers(oauthRequestHeader.build())
    .build()
```
