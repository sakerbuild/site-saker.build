# Accessing the repository runtime

When developing Java classes that are loaded by the repository runtime, you may occasionally access the repository runtime and its functionality. You can do that through the `ClassLoader` of a class that was loaded by the runtime.

Code sample for a class that has been loaded by the saker.nest repository:

```java
import saker.nest.bundle.NestBundleClassLoader;

NestBundleClassLoader nestcl = (NestBundleClassLoader) this.getClass()
	.getClassLoader();
// ... work with the repository through the class loader
```

Any class that is loaded by the repository runtime will have a classloader associated with them that implements the [`NestBundleClassLoader`](/javadoc/saker/nest/bundle/NestBundleClassLoader.html) interface. Throught this interface you can access the current configuration of the repository, access the contents of the enclosing bundle or other bundles, perform dependency resolutions, bundle lookups, and other operations.

See the [Javadoc](/javadoc/index.html) of the saker.nest client library for more information.
