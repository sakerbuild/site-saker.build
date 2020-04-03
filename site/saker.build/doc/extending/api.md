# Build system API

Saker.build provides a public Java API for the extensions to use. This API is published in a separate distribution than the main build system implementation. This separate distributions contains only the stubs of the publicly available classes and their members. It cannot be used to run it, but it should be used to develop your extensions agains.

The API distribution contains the APIs that the saker.build system authors take responsibility for. These APIs are tracked between releases, and are governed under the [change policy](#api-change-policy) of the build system.

If you're not using this API distribution to create your extensions, you might accidentally use non-public API, which may breakingly change without notice in any upcoming versions of the build system, therefore breaking your implementation.

## Using the API distribution

The API distribution of the build system is a Java Archive that contains stub class files with the public API of the build system. These stubs don't have any implementation, and this distribution cannot be used during runtime. It is much smaller than the main saker.build JAR distribution, and is designed to be the classpath input for Java compilation.

All of the accessible classes, methods, and fields in the API distribution is considered to be the public API of the build system.

The API distribution is available through the saker.nest repository with the bundle identifier [`saker.build-api`](https://nest.saker.build/package/saker.build). You can pass it to the Java compilation build tasks as an input. See [](root:/saker.java.compiler/doc/examples/nestbundleclasspath.html) for example.

Note that you **mustn't** declare the `saker.build` package as a dependency when publishing saker.nest bundles, as it is automatically added when the bundles are loaded.

## API change policy

<div class="doc-wip">

The manner of determining the API change policy is still work in progress. In general, it should be similar to the versioning specification by [semver.org](https://semver.org/). 

</div>

## Handling different versions

When building against the API of saker.build, you may handle the version requirements against the build system in various ways. Statically or dynamically.

#### Static versioning

In this case, the classes of your extension may only be loaded if the version of the build system matches the specified requirements. When creating a bundle for the saker.nest repository, you may the version ranges that your bundle can be loaded on.

You can use the `Nest-ClassPath-Supported-Build-System-Versions` [manifest file](root:/saker.nest/doc/devguide/bundleformat.html#manifest-file) attribute to specify the applicable build system versions.

**Note** that if you choose to use static versioning, your bundle or extension may not be able to load in build system versions that are out of the specified range. Make sure to examine the risks that these incompatibilities may incur.

#### Dynamic versioning

Dynamic versioning means that you determine the current version of the build system during runtime, and execute different code paths based on it.

The [`Versions`](/javadoc/saker/build/meta/Versions.html) class in the build system contains the version information of the current distribution. You can query the fields of this class during runtime, and execute different code based on it:

```java
TaskContext taskcontext;
if (Versions.VERSION_MAJOR >= 2) {
	taskcontext.methodIntroducedInV2();
} else {
	taskcontext.fallbackMethodBeforeV2();
}
```

The above will call the `methodIntroducedInV2()` method only if the current build system major release version is at least 2. If your code is running on an older version, the fallback branch will be executed.

The JVM has the advantage that it will *not* throw an error if a method doesn't exist if you don't actually call that method. Therefore, running the above code on versions before and after major 2 will work appropriately.

**Important!** You **must** compile your extension against the API distribution for this to work. The fields in the [`Versions`](/javadoc/saker/build/meta/Versions.html) class are `final`, and they will be inlined by the Java compiler if you compile against the saker.build runtime distribution, resulting in the if-else condition being optimized out. However, the API distribution doesn't declare these fields as `final`, and the Java compiler will not optimize away the branching.
