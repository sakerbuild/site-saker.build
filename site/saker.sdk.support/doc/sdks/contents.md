# SDK contents

What are the contents of an SDK from the build system perspective?

**An SDK is the collection of paths and properties**. The paths can be used by build tasks to retrieve the locations of various SDK resources. The properties can be used to determine various aspects of the given SDK (e.g. version).

Both the paths and properties are referenced using *identifiers*. These identifiers are defined by each different SDK types. The identifiers should be used in association with a given SDK.

For example:

* The `exe.java` path identifier for a Java SDK from the [saker.java.compiler](root:/saker.java.compiler/index.html) package will return the path to the Java executable.
* The `java.major` property identifier for a Java SDK from the [saker.java.compiler](root:/saker.java.compiler/index.html) package will return the value of the Java major version for the JDK.
* The `exe.cl.x64.x64` path identifier for a VC tools SDK from the [saker.msvc](root:/saker.msvc/index.html) package will return the path to the C++ compiler that runs on `x64` and compiles for `x64` architecture.

We can see that the identifiers that can be used with a given SDK depends on the SDK that is used to resolve the identifier. E.g. you can't use `exe.cl.x64.x64` identifier on a Java SDK, as a JDK doesn't provide such services.
