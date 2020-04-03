# SDK management

The [`saker.java.compile()`](/taskdoc/saker.java.compile.html) tasks allows specifying SDKs for the `SDKs` parameter. The usage of SDKs are based on the [saker.sdk.support](root:/saker.sdk.support/index.html) package.

SDKs are used for specifying the Java Development Kit that should be used for compilation, and occasionally specifying classpath and other resources for compilation. The [`saker.java.compile()`](/taskdoc/saker.java.compile.html) task specially handles the `Java` SDK. It will use that to execute the Java compilation.

```sakerscript
saker.java.compile(
	SourceDirectories: src,
	SDKs: {
		Java: saker.java.sdk(13)
	}
)
```

The above example shows that the task will use a JDK with the major version 13 to compile the Java sources in the `src` directory.

You can use the `saker.java.sdk()` task to request a JDK with a specific version.

If no `Java` SDK is specified, the one that the build execution is running on will be used.

## JDK install environment parameter

The `saker.java.sdk()` task discovers the installed Java runtimes based on the `saker.java.jre.install.locations` build environment user parameter. If you want to use a specific JDK, you'll need to add it using the following environment parameter:

```plaintext
saker.java.jre.install.locations=c:\Program Files\Java\jdk1.8.0_221
```

This will cause the task to check the specified location for a valid Java installation. It will determine the version of that installation and use it accordingly based on the requested version(s).

You may specify multiple Java installations in which case the environment parameter value must be a semicolon (`';'`) separated list of install locations.
