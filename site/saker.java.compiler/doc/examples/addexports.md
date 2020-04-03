# Add module exports

Starting from Java 9, when developing for the module system, you may not be able to access some internal classes in the JDK. If you're getting compilation errors due to encapsulation, you can use the `AddExports` parameter to force exporting of packages.

The `AddExports` parameter corresponds to the --add-exports command line parameter for `javac`.

```sakerscript
saker.java.compile(
	SourceDirectories: src,
	AddExports: {
		Module: jdk.compiler,
		Package:[
			com.sun.tools.javac.jvm,
			com.sun.tools.javac.main
		],
		Target: example.module
	},
)
```

In the above example we've added the exports from the `jdk.compiler` module of the `com.sun.tools.javac.jvm` and `com.sun.tools.javac.main` packages to the `example.Module` module.

The `Target` property can also accept multiple module names. If the `Target` property is omitted, the packages will be exported for all unnamed modules.

The parameter also accepts simple string based input in the format of `module/package=other-module(,other-module)*`. If `=other-module...` is not provided, the package is exported to all unnamed modules:

```sakerscript
saker.java.compile(
	SourceDirectories: src,
	AddExports: [
		jdk.compiler/com.sun.tools.javac.jvm=example.module,
		jdk.compiler/com.sun.tools.javac.main=example.module,
	],
)
```

This definition corresponds to the command line syntax of `--add-exports`.
