# Bundle classpath

This document describes how the classpath loading of the bundles work.

As the first step of loading classes from a bundle, the repository runtime will check if it is suitable for the [current environment](../userguide/constraints.md) to run on. It will examine the `Nest-ClassPath-Supported-*` attributes of the [bundle manifest](bundleformat.md#manifest-file), and check if it allows the bundle to be loaded in the current environment. If it is not suitable, an appropriate exception is thrown by the runtime.

As the second step, the runtime will resolve the declared dependencies of the bundle. The dependencies are resolved fro the `classpath` kind. Any dependency that is declared with this kind will be used to resolve the dependencies of bundles in a transitive way. During resolution, the meta-data specifications mentioned in [](dependencies.md#dependency-meta-data) will be taken into account. For every dependent bundle, the current environment suitability will also be checked.

As a last step, the `Nest-ClassPath-Special` manifest attribute of the bundle is taken into account to create the [`ClassLoader`](/javadoc/saker/nest/bundle/NestBundleClassLoader.html) for the bundle. If necessary, the JDK tool classes will be made accessible for the bundle classes.

Some important aspects of the class loading is discussed below.

## Bundle lifecycle

Contrary to other package management solutions, there's **no bundle lifecycle** defined for saker.nest packages. Your Java classes are not notified when the bundle is first loaded, or when it is unloaded. *Bundles shouldn't hold references to managed resources.*

A bundle should be used as a container of resources, not some service provider.

## Multiple ClassLoaders

Important to note that a single bundle can be loaded **multiple times** by the repository runtime. This is due to having different bundle dependency resolutions in some cases. Let's look at the following example:

```plaintext
first.bundle-v1         second.bundle-v1
|            |           |            |
| 1.0        | 1.0       | 1.0        |
|            |           |            |
|            V           V            |
|         dependent.bundle-v1.0       | 2.0
|            |           |            |
|            | [1, 3]    | [1, 3]     |
|            |           |            |
V            V           V            V
common.bundle-v1.0      common.bundle-v2.0
```

The bundles define the dependencies for the given bundles with the specified [version ranges](../userguide/versioning.md#version-ranges). If we resolve the dependencies for `siple.bundle-v1`, we will have the following result:

```plaintext
first.bundle-v1
dependent.bundle-v1.0
common.bundle-v1.0
```

However, if we resolve the dependencies for `second.bundle-v1`, we will have the following:

```plaintext
second.bundle-v1
dependent.bundle-v1.0
common.bundle-v2.0
```

Let us look at the class accessibility of `dependent.bundle-v1.0`. In the first case, the dependent bundle has access to the `common.bundle-v1.0` classes. However, in the second case, the `common.bundle-v2.0` was resolved, because `second.bundle-v1` pinned that specific version.

In this case, two *completely different* ClassLoader is instantiated for `dependent.bundle-v1.0`. This results in that classes from the dependent bundle being loaded into the runtime **multiple times**. This is the scenario commonly known as dependency hell or [JAR hell](https://en.wikipedia.org/wiki/Java_Classloader#JAR_hell).

The saker.nest repository does not attempt to solve this problem on its own, but rather provides development guidelines that help mitigating this issue. It requires employing specific development practices by the package authors to avoid dealing with this issue.

See [](dependencyhell.md) for our recommendations.

## Native libraries

The repository runtime supports loading native libraries contained in the bundle. When you call [`System.loadLibrary()`](https://docs.oracle.com/javase/8/docs/api/java/lang/System.html#loadLibrary-java.lang.String-), the class loader for the bundle will automatically extract the contained native library if present in the bundle.

The library extraction mechanism is as follows:

1. The dot (`'.'`) separators are converted to forward slashes (`'/'`).
2. The current [native architecture](../userguide/constraints.md#native-architecture) environment value is appended to the library name separated using a dot (`'.'`).
3. The file name is mapped to an OS specific name using [`System.mapLibraryName()`](https://docs.oracle.com/javase/8/docs/api/java/lang/System.html#mapLibraryName-java.lang.String-).
4. If an entry exists in the bundle for the resulting path, it is extracted and loaded.
5. Else the runtime will map the library name without the appended native architecture and try loading it.
6. If the bundle entry still not found, the extraction fails.

For example, the `path.to.MyLibrary` on Linux systems, on `amd64` architecture will be extracted the following way:

1. If `path/to/libMyLibrary.amd64.so` is present, it is extracted.
2. Else if `path/to/libMyLibrary.so` is present, it is extracted.
3. In other cases the extraction fails.

See the following Stack Overflow question for some information about `System.mapLibraryName()`: [While loading JNI library, how the mapping happens with the actual library name](https://stackoverflow.com/questions/37203247)

**Important**: As seen in [](#multiple-classloaders), there may be scenarios when classes in a bundle is loaded multiple times. In these cases this may cause that a given native library needs to be loaded multiple times. However, a single native library **cannot be loaded multiple times** in a JVM process. Doing so will cause an `UnsatisfiedLinkError` to be thrown by the JVM when the library is attempted to be loaded the second time. See the dependency hell [](dependencyhell.md#native-libraries) section for mitigating this issue.

