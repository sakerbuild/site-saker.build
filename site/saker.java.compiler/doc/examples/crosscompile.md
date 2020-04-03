# Cross-compilation

When cross-compiling Java sources for a different version, you have two options to achieve that. Either specify the target version of the generated class files, or execute the compilation using a different JDK.

If you're attempting to compile for a more recent Jave version than the one the build is currently execution on, then you must use a different JDK, as the current one won't recognize the target version.

### Older target version

The following example showcases cross-compiling for an older Java version:

```sakerscript
saker.java.compile(
	SourceDirectories: src,
	SourceVersion: 7,
	TargetVersion: 7,
	Parameters: [ --release, 7 ]
)
```

The above example compiles the sources in the `src` directory for Java 7.

We need to specify all of the above parameters (`SourceVersion`, `TargetVersion`, `--release 7` parameters) so the compilation task works properly no matter what Java runtime the build is running on.

If the build runs on Java 8, then the `--release 7` parameters won't be passed to the compiler and the source and target parameters specify the target. If the build runs on more recent versions of Java (9+), then the `--release 7` ensures that the sources are compiled against an appropriate JVM API.

### Forked compilation

If you want to compiler for more recent Java versions than the one the current build is running on, you'll need a JDK installation for that version, and use forked compilation.

This can be done by specifying a different JDK for the compilation task to use:

```sakerscript
saker.java.compile(
	SourceDirectories: src,
	SDKs: {
		Java: saker.java.sdk(13)
	}
)
```

The above example will find a JDK installation with the major version of 13 and use that to compile the Java sources in the `src` directory.

The JDK to be used can be specified with the [SDK mechanism](root:/saker.sdk.support/doc/sdks/index.html), and an appropriate JDK installation can be found using the [`saker.java.sdk()`](root:/saker.java.compiler/taskdoc/saker.java.sdk.html) task.

In this case you don't need to specify the source version, target version, and `--release` parameters, as the version 13 will be default for them. However, if you still want to specify them, you may do so.

Note that in this case, if the current build is already running on Java 13, the compilation will not use process forking.

Also note that forked compilation may be slower than in-process compilation.
