# Bundle dependencies

To resolve dependencies on bundles, you can specify them as the input of the [`nest.dependency.resolve()`](/taskdoc/nest.dependency.resolve.html) task;

```sakerscript
nest.dependency.resolve([
	first.bundle,
	second.bundle-api,
	third.bundle-v1.0,
])
```

In the above we resolve all the dependencies of the specified bundles, including transitive ones. (I.e. the dependencies of the dependencies, and so on.)

You may want to resolve only dependencies with a specific kind. In order to do that, specify a dependency `Filter`:

```sakerscript
nest.dependency.resolve(
	[
		first.bundle,
		second.bundle-api,
		third.bundle-v1.0,
	],
	Filter: nest.dependency.filter.kind(classpath)
)
```

In this case only the dependencies for the kind `classpath` will be resolved.

After resolving the dependencies, you can pass them to other tasks to consume. E.g. [](javacompileclasspath.md).
