# Nest bundle classpath

If you want to use a saker.nest bundle as an input classpath to the compilation, you can use the [`saker.java.classpath.bundle()`](/taskdoc/saker.java.classpath.bundle.html) task to create a classpath for them. The task will automatically retrieve any source bundles if you're building inside an IDE:

```sakerscript
saker.java.compile(
	SourceDirectories: src,
	ClassPath: saker.java.classpath.bundle(example.bundle-v1.0)
)
```

The above will use the `example.bundle-v1.0` bundle as the compilation classpath. **Note** that the task doesn't perform any dependency resolution. In order to do that, you may pass the output of the [`nest.dependency.resolve()`](root:/nest.repository.support/taskdoc/nest.dependency.resolve.html) task as an input:

```sakerscript
saker.java.compile(
	SourceDirectories: src,
	ClassPath: saker.java.classpath.bundle(
		nest.dependency.resolve(example.bundle-v1.0)
	)
)
```

The above will have the `example.bundle-v1.0` bundle and **all** of its dependencies as the input classpath. However, in some cases you don't want all the dependencies to be present on the classpath.

When you depend on bundles that have its classes separated by API and implementation, then you probably don't want the implementation bundle to be on the classpath, but only the API. You can use the [compile dependency filter](root:/nest.repository.support/doc/examples/compilefilter.html) to filter out the unnecessary transitive dependencies:

```sakerscript
saker.java.compile(
	SourceDirectories: src,
	ClassPath: saker.java.classpath.bundle(
		nest.dependency.resolve(
			example.bundle-v1.0,
			Filter: nest.dependency.filter.compile(CompileTransitive: false)
		)
	)
)
```

This way, the dependencies which shouldn't be transitive for Java compilation will be filtered out accordingly. See the [](root:/nest.repository.support/doc/examples/compilefilter.html) example in the [nest.repository.support](root:/nest.repository.support/doc/index.html) package for more information about the compile dependency filter.
