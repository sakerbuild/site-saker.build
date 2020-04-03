# Stream utilities

The [`StreamUtils`](/javadoc/saker/util/io/StreamUtils.html) utility class provides functionality for working with streams and related classes.

It consuming the data from streams in various ways. These include like reading all bytes from a stream, copying streams, skipping bytes from them.

The class allows handling classes like [`MessageDigest`](https://docs.oracle.com/javase/8/docs/api/java/security/MessageDigest.html) and [`Signature`](https://docs.oracle.com/javase/8/docs/api/java/security/Signature.html) as output streams, easing their usage.

Other wrappers that synchronize the access to streams, or prevents their closing are also available.

The class also provides various null reading and writing streams that doesn't do anything. 
