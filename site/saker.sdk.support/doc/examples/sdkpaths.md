# SDK paths

SDK paths can be referenced using the [`sdk.path()`](/taskdoc/sdk.path.html) task. This example is based on the [](root:/saker.java.compiler/doc/examples/jnicompile.html) example from the [saker.java.compiler](root:/saker.java.compiler/index.html) package.

```sakerscript
$javac = saker.java.compile(
	# ...
)
saker.msvc.ccompile(
	{
		Files: csrc/**/*.cpp,
		IncludeDirectories: [
			sdk.path(java, Identifier: include),
			sdk.path(java, Identifier: include.platform),
			$javac[HeaderDirectory]
		]
	},
	SDKs: {
		Java: $javac[JavaSDK]
	}
)
```

In the above example we can see that for the [`saker.msvc.ccompile()`](root:/saker.msvc/taskdoc/saker.msvc.ccompile.html) task, we specify the `Java` SDK as an input, and add include directories (`IncludeDirectories`) that are path references from the Java SDK.

When the compiler task interprets the include directories, it will see that they are specified to be resolved against an SDK with the `java` name. It will retrieve the SDK reference for the `java` name, and use it to resolve the SDK paths with the identifier `include` and `include.platform`.

In the end this results in the include directories being configured based on the Java SDK, and the source files will be able to use the `jni.h` and related header files.

Note that the SDK names are handled in an case-insensitive manner. See [](../sdks/sdknames.md) for more information.
