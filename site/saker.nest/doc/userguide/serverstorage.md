# Server storage

A server storage provides acces to bundles available from the saker.nest repository server. When a bundle for a given identifier is requested from it, it will look up using the bundle index provided by the server, and download the appropriate bundle.

The storage caches the downloaded bundles locally, and won't make unnecessary network requests. The server storage also validates the bundle contents to ensure that they've been downloaded from the appropriate server. This is done for security reasons.

The storage can be configured to communicate with different servers, to operate in offline mode, or change the signature verification mechanism.

The following sections list the possible parameters for the server storage. All parameters are to be interpreted in the `<repo-id>.<storage-name>.<parameter>` format defined in the [](configuration.md) document.

## `url`

Specifies the server URL to be used by the storage to access bundles of the repository.

The default value is `https://api.nest.saker.build`.

In some case the [secondary URL](#url_secondary) can take precedence when issuing requests for load balancing purposes.

The specified URL shouldn't have a slash separator at the end.

## `url.secondary`

Specifies the secondary URL that can be used for load balancing when making network requests.

The secondary URL is used first to retrieve the bundle and task indexes from the server. As they might be often queried by clients, the secondary URL help taking the load off the primary server and lets it deal with other requests.

If the request to the secondary URL fails, the request will be tried again from the primary server.

Bundle downloads always use the primary URL.

The specified URL shouldn't have a slash separator at the end.

Secondary URL can be disabled by setting the empty or `"null"` value to it.

## `offline`

Specifies if the server storage should operate in offline mode.

If the storage is set to operate in offline mode, any operations that require making network requests will result in an exception. The storage is still safe to operate on locally cached bundles and indexes.

## `signature.verify`

Specifies if the bundle signature verification should be disabled by the storage.

If the value of this parameter equals to `"false"` in a case-insensitive manner, the storage will
**not** verify the downloaded bundle integrity, and will load them without verifying that they've been
downloaded from the expected server.

In general, developers shouldn't disable the signature verification, as errors in the verification process either
signal an internal implementation error, security breach, or other serious issue with the supporting environment.
In case you feel the need to disable it, it is recommended to throughly examine the cause of error and verify
that it is acceptable to impose the security risk caused by the lack of verification.

## `signature.version.min`

Parameter setting the minimum accepted server provided signature version.

The expected value of this parameter is an integer that specifies the minimum version.

By default, the verification mechanism only accepts the latest signature version for a downloaded bundle.
Lowering the minimum accepted version may result in increased security risk as it may allow the loading of
bundles that were signed with a compromised private key.

Newer releases of the Nest repository runtime will always use the signature version that is the most recent at
the time of release. However, there may be cases when the signature version is increased by the server and older
releases of the repository needs to be used by the developer. In these cases it is recommended to use the
parameter to <i>increase</i> the minimum accepted signature version to the one that is provided by the server.
This will prevent loading cached bundles that was signed with signature version more recent than the repository
runtime release, but older than the version provided by the server.

In general, if the value of this parameter needs to be set below the version that is currently advertised by the
server, that can signal serious issues in relation of security or internal implementation. Make sure to examine
throughly the possible security consequences of such action.

