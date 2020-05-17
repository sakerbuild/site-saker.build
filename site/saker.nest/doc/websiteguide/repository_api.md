# Repository web API

<div class="doc-wip">

This section of the documentation is incomplete. 

</div>

The saker.nest repository website at [https://nest.saker.build](https://nest.saker.build) consists of two parts. A frontend that is displayed in the browser, and a backend that is the database and logic behind the site. This page describes the API that can be used to communicate with the backend server.

The API is available only via HTTPS and through the `https://api.nest.saker.build` address.

<div class="doc-note">

The site at [https://nest.saker.build](https://nest.saker.build) uses other API endpoints that are not part of this document. They are subject to change at any time. You should only rely on API functions that are described in this document.

</div>

## Requests types

The API server distinguises two kinds of requests.

### Unauthenticated requests

These kinds of requests require no authentication by the client.

### Authenticated requests

These requests need to be signed by the client using the API keys previously provided by the server. Authenticated requests need to be formatted in a way that can be interpreted by the server.

The API keys are a pair of byte sequences that are used to authenticate the client. It consists of an API key and API secret. You shouldn't share these keys with anyone. Most commonly, you'll be using the API keys for uploading bundles for your packages.

Example:

```
Key: YWJjZGVmZ2hpamtsbW5vcHFyc3R1dnd4eXoxMjM0NTY
Secret: NjU0MzIxenl4d3Z1dHNycXBvbm1sa2ppaGdmZWRjYmE
```

As an example, the above keys can be retrieved from the Bundles tab at [https://nest.saker.build/user/packages](https://nest.saker.build/user/packages). The above keys are invalid, and serve only as an example.

#### Request format

An authenticated request consists of the following:

* The API key. (**Not** the *Secret*.)
* The request contents.
* The request signature.

In order to construct a request, you need to sign the contents of the request. The signed contents consist of the UTF-8 encoded bytes of the following (in this order):

* Request HTTP method (POST, GET, ...)
* Full request URL
* The API Key (URL-safe Base64 (RFC 4648 Section 5) encoded string *without padding*)
* The request contents

The signing algorithm is: `HmacSHA256`. The API Secret is the key for the signing.

The actual API key and signature is passed in the request as the following headers:

* `NestAPIKey`: The URL-safe Base64 (RFC 4648 Section 5) encoded string of the API Key. (**Not** the secret, only the key.)
* `NestRequestMAC`: The URL-safe Base64 encoded string of the signature produced as above.

See also: [`Base64.getUrlEncoder()`](https://docs.oracle.com/javase/8/docs/api/java/util/Base64.html#getUrlEncoder--).

The following Java snippet can be used to initialize a request:

```java
// (exception handling omitted)
byte[] apikey; // decoded to raw bytes
byte[] apisecret; // decoded to raw bytes
// URL-safe Base64 encoder without padding
Encoder base64encoder = Base64.getUrlEncoder().withoutPadding();

// example request contents 
String httpmethod = "POST";
String url = "https://api.nest.saker.build/...";
String apikeybase64 = base64encoder.encodeToString(apikey);
String requestcontents = "{ contents: \"of-the-request\" }";

Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
sha256_HMAC.init(new SecretKeySpec(apisecret, "HmacSHA256"));
sha256_HMAC.update(httpmethod.getBytes(StandardCharsets.UTF_8));
sha256_HMAC.update(url.getBytes(StandardCharsets.UTF_8));
sha256_HMAC.update(apikeybase64.getBytes(StandardCharsets.UTF_8));
sha256_HMAC.update(requestcontents.getBytes(StandardCharsets.UTF_8));
byte[] macbytes = sha256_HMAC.doFinal();

HttpURLConnection urlconn = (HttpURLConnection) new URL(url).openConnection();
urlconn.setRequestMethod(httpmethod);
urlconn.setRequestProperty("NestAPIKey", apikeybase64);
urlconn.setRequestProperty("NestRequestMAC", base64encoder.encodeToString(macbytes));
urlconn.setDoOutput(true);
try (OutputStream os = urlconn.getOutputStream()) {
	os.write(requestcontents.getBytes(StandardCharsets.UTF_8));
}
```

## Bundle downloading

*Unauthenticated.*

```
GET https://api.nest.saker.build/bundle/download/<bundle-id>
```

Allowed methods: `GET`, `POST`, `HEAD`

The `bundle-id` parameter contains the full bundle identifier for the downloaded bundle including the version qualifier.

**Contents**

The request contains no contents.

**Response**

The binary byte stream of the bundle contents or an error describing the issue.

The server may also respond with a redirection status code in which case the download should be attempted from that address.

## Bundle upload allocation

**Authenticated.**

Asks the server to allocate the resources for an upcoming bundle upload.

```
POST https://api.nest.saker.build/bundle/upload/allocate?bundleid=<bundle-id>&overwrite=<bool>
```

Allowed methods: `POST`

The `bundle-id` parameter contains the full bundle identifier for the uploaded bundle including the version qualifier.

The `overwrite` optional parameter specifies whether or not the already uploaded bundle can be overwritten.

**Contents**

The request contains no contents.

**Response**

HTTP 200 with JSON response that contains the URL to where the bundle uploading can proceed.

Example:

```json
{
	"error": "success",
	"uploadurl": "https://api.nest.saker.build/url/where/you/can/upload/the/bundle"
}
```

The server can respond with errors in cases as follows:

* You don't have the permission to upload a bundle for that package.
* The version to which the upload would be performed was already published.
* For other reasons.

The server may also respond with HTTP 503, in which case you should try the request again after the timeout specified in the `Retry-Again` header. In this case the response contains the `"try-again"` error code and the `"seconds"` field also contains the timeout length.

See the [next chapter](#bundle-uploading) for performing the bundle upload.

## Bundle uploading

*Unauthenticated.*

Uploads the bundle to the URl retrieved from [](#bundle-upload-allocation).

```
POST https://api.nest.saker.build/url/that/you/received/after/allocation
```

Allowed methods: `POST`

**Contents**

The contents should be a single bundle in [`multipart/form-data`](https://tools.ietf.org/html/rfc2388) format.

**Response**

HTTP 200 if the bundle uploading was successful. The server will proceed to validate the bundle contents and the status will be displayed on the Bundles tab of the [bundle management page](https://nest.saker.build/user/packages).

## Bundle index

<div class="doc-wip">

This section of the documentation is incomplete. 

</div>

## Task index

<div class="doc-wip">

This section of the documentation is incomplete. 

</div>
