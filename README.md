# OAuth Header

An OkHttp helper for generating the three bits needed for 3 legged OAuth. 

1. Request Token Request building
2. Authorize URL building
3. Access Token Request building

## Example

### Step 1: Getting Request Token

Define the key and secret generated for your app, by the Service

```kotlin
val consumerKey = "YOUR_CONSUMER_KEY"
val consumerSecret = "YOUR_CONSUMER_SECRET"
```

Empty for this step since we haven't fetched anything yet

```kotlin
val accessToken = ""
val accessSecret = ""
```

Your request for a resource on Service, that will require OAuth

```kotlin
val body = FormBody.Builder()
    .add("status", "Hello Ladies + Gentlemen, a signed OAuth request!")
    .build()
val resourceReq = Request.Builder()
    .url("https://api.twitter.com/1/statuses/update.json?include_entities=true")
    .post(body)
    .build()
```

