# Classpath

You can add saker.rmi as the classpath for your project. You'll need to resolve the bundles during build execution and set it to the classpath of your task. An example for setting it for compilation:

```sakerscript
saker.java.compile(
	ClassPath: saker.java.classpath.bundle(
		nest.dependency.resolve(
			saker.rmi,
			Filter: nest.dependency.filter.compile(CompileTransitive: false)
		)
	)
)
```

The above will resolve the most recent version of saker.rmi and use it as the Java compilation input classpath. See [](root:/saker.java.compiler/doc/examples/nestbundleclasspath.html) for more information.

If you don't want to use the runtime of the library, you can use the `saker.rmi-api` dependency for resolution.
