# Bundle classpath

The package provides facility for creating a classpath that contains saker.nest bundles. The [`saker.java.classpath.bundle()`](/taskdoc/saker.java.classpath.bundle.html) task allows creating classpath for a given set of bundles. If the build is running inside an IDE, then the source attachments will be automatically downloaded byte the task.

```sakerscript
saker.java.compile(
	SourceDirectories: src,
	ClassPath: saker.java.classpath.bundle(example.bundle-v1.0)
)
```

The above compilation will have the `example.bundle-v1.0` on its classpath. **Note** that the dependencies of the bundle are not resolved. To resolve them, use the [`nest.dependency.resolve()`](root:/nest.repository.support/taskdoc/nest.dependency.resolve.html) task:

```sakerscript
saker.java.compile(
	SourceDirectories: src,
	ClassPath: saker.java.classpath.bundle(
		nest.dependency.resolve(example.bundle)
	)
)
```

The above will include the `example.bundle` with an appropriate version as well as its dependencies (including transitive ones). 
