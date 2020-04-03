# Transparency

One of the core features of Saker.rmi is that the RMI proxy objects should work the same way as if they weren't proxy objects. The code that uses an RMI connection should work the same way if it wasn't using the connection. The fact that an object is behind a proxy shouldn't impose any restrictions on the behaviour of the proxy and the code that uses it.

An example that showcases this is by working with files using an interface:

```java
public interface FileSystemStub {
	public byte[] getFileBytes(String filepath) throws IOException;
}

public class FileSystemImpl implements FileSystemStub {
	@Override
	public byte[] getFileBytes(String filepath) throws IOException {
		return Files.readAllBytes(Paths.get(filepath));
	}
}
```

The above defines an interface that provides access to the contents of a file for a given path. Below is a basic implementation for that class.

The code that uses the `FileSystemStub` interface doesn't have to work differently if the given interface is backed by an RMI proxy. The following code should *always* be correct irregardless of the backing implementation of the `FileSystemStub`:

```java
public static String[] getFileLines(FileSystemStub fs, String filepath) 
		throws IOException {
	return new String(fs.getFileBytes(filepath)).split("\n");
}
```

If the `fs` argument is a remote object, the above method still works correctly, and needs no special handling. Although this example is overly simplified, accessing remote file system like this is an important use-case for the RMI library.

## Exceptions

What happens when the RMI connection breaks up, or otherwise fails? The proxy method calls will throw an appropriate [`RMIRuntimeException`](/javadoc/saker/rmi/exception/RMIRuntimeException.html). You may want to choose to catch it, in which case the code needs special handling. However, you may also hard fail in case of network errors if that seriously distrupts application workflow, or configure the RMI connection to handle it. The RMI library may rethrow an exception if it is caused by connection failures. You can annotate the method using the [`@RMIExceptionRethrow`](/javadoc/saker/rmi/annot/invoke/RMIExceptionRethrow.html) annotation:

```java
public interface FileSystemStub {
	@RMIExceptionRethrow(RemoteIOException.class)
	public byte[] getFileBytes(String filepath) throws IOException;
}
```

In this case, if the method call fails due to an RMI connection error (unexpectedly breaking up, or others), then an instance of [`RemoteIOException`](root:/saker.util/javadoc/saker/util/io/RemoteIOException.html) will be thrown instead of an RMI runtime error. This way, the transparency that we've been striving for since the beginning can be restored.
