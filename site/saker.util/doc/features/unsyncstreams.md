# Non-synchronized streams

You may know that some of the stream classes in the Java standard library have `synchronized` implementations. Some examples are: [`ByteArrayOutputStream`](https://docs.oracle.com/javase/8/docs/api/java/io/ByteArrayOutputStream.html), [`BufferedOutputStream`](https://docs.oracle.com/javase/8/docs/api/java/io/BufferedOutputStream.html), [`BufferedInputStream`](https://docs.oracle.com/javase/8/docs/api/java/io/BufferedInputStream.html), and others.

The library provides non-synchronized implementations of similar streams that perform the same (and additional) functionality. E.g. [`UnsyncByteArrayOutputStream`](/javadoc/saker/util/io/UnsyncByteArrayOutputStream.html), [`UnsyncBufferedInputStream`](/javadoc/saker/util/io/UnsyncBufferedInputStream.html) and others.
