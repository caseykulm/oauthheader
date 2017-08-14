# OAuth Header

An OkHttp helper for generating the three bits needed for 3 legged OAuth. 

1. Request Token Request building
2. Authorize URL building
3. Access Token Request building

## Example

### Step 0: Define your OAuth constants

Define the key and secret generated for your app, by the Service.

```kotlin
val consumerKey = "YOUR_CONSUMER_KEY"
val consumerSecret = "YOUR_CONSUMER_SECRET"
```

Define the 3 OAuth endpoints from the Service.

```kotlin
val requestTokenUrl = "https://api.twitter.com/oauth/request_token"
val authorizeUrl = "https://api.twitter.com/oauth/authorize"
val accessTokenUrl = "https://api.twitter.com/oauth/access_token"
```

### Step 1: Getting Request Token

Access Token and Access Secret Empty for this step since we 
haven't fetched any tokens yet.

```kotlin
val accessToken = ""
val accessSecret = ""
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
