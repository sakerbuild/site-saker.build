# Dependency constraints

When resolving dependencies, the [dependency constraints](root:/saker.nest/doc/userguide/constraints.html) can be configured for the [`nest.dependency.resolve()`](/taskdoc/nest.dependency.resolve.html) task.

```sakerscript
nest.dependency.resolve(
	DependencyFile: res/META-INF/nest/dependencies,
	DependencyConstraints: {
		JREMajorVersion: 9
	}
)
```

If the `res/META-INF/nest/dependencies` file contains the following:

```plaintext
example.bundle-jre8
	classpath: 1.0
		jre-version: 8
example.bundle-jre9
	classpath: 1.0
		jre-version: 9
example.bundle-jre10p
	classpath: 1.0
		jre-version: [10)
```

Then the above script will resolve the `example.bundle-jre9` dependency and omit the others. You can also specify other constraints using the fields specified [here](/taskdoc/types/DependencyConstraintsTaskOption.html). The associated dependency meta-data names are explained [here](root:/saker.nest/doc/devguide/dependencies.html#dependency-meta-data).
