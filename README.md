# OAuth Header

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.caseykulm.oauthheader/oauthheader-parent/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.caseykulm.oauthheader/oauthheader-parent)

An OkHttp helper for generating the four bits needed for 3 legged OAuth. 

1. Request Token
2. Authorize
3. Access Token
4. Resource Request Signing oauthheader 

## Download 

With Gradle:

```groovy
dependencies {
  compile 'com.caseykulm.oauthheader:oauthheader:0.4.0'
  compile 'com.caseykulm.oauthheader:oauthheader-parent:0.4.0' 
}
```

With Maven: 

```xml
<dependency>
  <groupId>com.caseykulm.oauthheader</groupId>
  <artifactId>oauthheader</artifactId>
  <version>0.4.0</version>
</dependency>
<dependency>
  <groupId>com.caseykulm.oauthheader</groupId>
  <artifactId>oauthheader-services</artifactId>
  <version>0.4.0</version>
</dependency>

```

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

### Step 1: Get a Request Token

Kick off the first request to get the request token which we will use later.

```kotlin
val requestTokenResponse: RequestTokenResponse = oauthClient.getRequestToken()
```

### Step 2: Get a formatted Authorization URL

This will form the url you should send your users to. If you set 
the callback url, the data for the next step will be sent there.

```kotlin
val authorizationUrl: String = oauthClient.getAuthorizationUrl(requestTokenResponse)
```

### Step 3: Intercept the Authorization Response

You should intercept the query string from the Oauth page 
calling your callback and it will look something like this

```kotlin
val rawQueryStr = "oauth_token=123abc&oauth_verifier"
```  

Then you should use this helper to parse the response to 
check if it is valid, and to pass as input to the next step.

```kotlin
val authorizationResponse: AuthorizationResponse = oauthClient.parseVerificationResponse(rawQueryStr)
```

### Step 4: Get a Access Token

Now you can get an access token.

```kotlin
val accessTokenResponse: AccessTokenResponse = oauthClient.getAccessToken(
  requestTokenResponse, authorizationResponse)
```

### Step 5: Getting a Signed Resource Request Header 

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

**OkHttp Interceptor to add header to each request**

You can now use the provided helper interceptor.

```kotlin
val oauthInterceptor = Oauth1Interceptor(oauth1Client, accessTokenResponse)
```

And add it to your OkHttpClient instance.

```kotlin
val oauthSessionClient = unauthedClient.newBuilder()
  .addInterceptor(oauthInterceptor)
  .build()
```

**Manually add header to each request**

Or you can manually get a signed header from the oauthClient, providing 
the stuff we've acquired so far.

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

And now you're good to send off an authenticated requests 🎉

## Steps after Authenticating Once

You need to persist both the 
```AccessTokenResponse``` and the ```AuthorizationResponse``` in order to 
avoid needing to do Steps 1-4 in the future.

Assuming you have those, you should just be able to perform Step 5 for 
every request to the service moving forward.

## Reauthorizing

Although the ```signedResourceRequest``` will usually work, you should 
expect that it can return 401 at any point, do to multiple reasons such 
as the user revoking your client apps access, your consumer values being 
revoked/expired, etc.

If that happens then you should proceed from Step 1.
