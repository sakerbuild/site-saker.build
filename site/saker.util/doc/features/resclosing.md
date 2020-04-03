# Resource closing

There are *many* cases when a class holds multiple managed resources that need to be closed when the class itself is closed. The [`IOUtils`](/javadoc/saker/util/io/IOUtils.html) class provides some utility functions that [close](https://docs.oracle.com/javase/8/docs/api/java/io/Closeable.html) the resources in a simpler way.

```java
public class MyResourceHolder implements Closeable {
	private InputStream input;
	private ZipFile zip;
	private RandomAccessFile file;
	
	@Override
	public void close() throws IOException {
		IOUtils.close(input, zip, file);
	}
}
```

In the above, we close the resources in the class using a single function call. If we didn't use that, we would've needed to write multiple try-catch blocks:

```java
try {
	input.close();
} finally {
	try {
		zip.close();
	} finally {
		file.close();
	}
}
```

And even in this case, if multiple `close()` calls fail, some of the exceptions may be not part of the actually thrown one.

There are workarounds for more cleaner code:

```java
try (InputStream input = this.input;
		ZipFile zip = this.zip;
		RandomAccessFile file = this.file) {
}
```

However, we feel that using [`IOUtils.close`](/javadoc/saker/util/io/IOUtils.html#close-java.lang.AutoCloseable...-) is a more cleaner way of describing our intent. 
