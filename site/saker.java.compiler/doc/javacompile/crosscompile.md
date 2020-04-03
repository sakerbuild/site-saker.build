# Cross-compilation

Cross-compilation is the act of compiling the sources to be used with a different version than the current environment. E.g. if we're using JDK 10 to compile the sources, but we plan on running them on Java 8, then we'll be cross-compiling.

The [`saker.java.compile()`](/taskdoc/saker.java.compile.html) task supports cross-compilation the same way as `javac` does. It allows you to specify the source and target versions, and the `--release` option for JDK 9 or later.

In the following example we compile the sources to run on Java 8.

```sakerscript
saker.java.compile(
	SourceDirectories: src,
	SourceVersion: 8,
	TargetVersion: 8
)
```

If we run the build on JDK 9 or later, then we may also need to specify the `--release` option for correctness:

```sakerscript
saker.java.compile(
	SourceDirectories: src,
	SourceVersion: 8,
	TargetVersion: 8,
	Parameters: [ --release, 8 ]
)
```

Note that the exact semantics are the same as the [`javac`](https://docs.oracle.com/javase/9/tools/javac.htm) tool. The `SourceVersion` and `TargetVersion` corresponds to the `-source` and `-target` command line arguments accordingly.

The `--release` parameter and its argument is ignored if the compilation is running on earlier versions than Java 9.

## Forked compilation

In cases when you're compiling for a more recent Java version that the one the build execution is running on, you may want to use forked compilation. In this case the [`saker.java.compile()`](/taskdoc/saker.java.compile.html) task will spawn a new `java` process that will execute the compilation.

The spawned Java process will run on the target JDK, and will communicate with the compiler task in order to do its work. Using forked compilation may be slower than using the in-process method. The incremental compilation and annotation processing should work the same way nonetheless.

Example for using JDK 13 to compile the Java sources:

```sakerscript
saker.java.compile(
	SourceDirectories: src,
	SDKs: {
		Java: saker.java.sdk(13)
	}
)
```

In order to setup the forked compilation, the task uses the [SDK mechanism](root:/saker.sdk.support/doc/sdks/index.html) to pass the configuration. The [`saker.java.sdk()`](root:/saker.java.compiler/taskdoc/saker.java.sdk.html) task finds an appropriate JDK reference with the major version of 13, and that will be used to compile the sources.

If the current build execution already runs on JDK 13, then the compilation will not be forked.
