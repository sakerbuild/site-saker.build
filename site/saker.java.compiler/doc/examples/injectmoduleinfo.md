# Injecting module-info attributes

The [`saker.java.compile()`](/taskdoc/saker.java.compile.html) task supports injecting the module version and main class attributes into the generated `module-info.class` file.

This only applies if you're compiling for Java 9 or later, and have a `module-info.java` source file with a module declaration.

```sakerscript
saker.java.compile(
	SourceDirectories: src,
	ModuleMainClass: example.Main,
	ModuleVersion: "1.0"
)
```

The above will cause the compiler task to inject the specified attributes into the compiled `module-info.class` file.

Note that it is important that you specify the version in quotes, as the simple `1.0` literal may be interpreted as a number by the build script.
