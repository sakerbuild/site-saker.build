# Classpath

You can add saker.util as the classpath for your project. You'll need to resolve the bundles during build execution and set it to the classpath of your task. An example for setting it for compilation:

```sakerscript
saker.java.compile(
	ClassPath: saker.java.classpath.bundle(
		nest.dependency.resolve(
			saker.util,
			Filter: nest.dependency.filter.compile(CompileTransitive: false)
		)
	)
)
```

The above will resolve the most recent version of saker.util and use it as the Java compilation input classpath. See [](root:/saker.java.compiler/doc/examples/nestbundleclasspath.html) for more information.
