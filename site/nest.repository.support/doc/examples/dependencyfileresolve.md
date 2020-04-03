# Resolve dependency file

The [`nest.dependency.resolve()`](/taskdoc/nest.dependency.resolve.html) task can be used to resolve the dependencies specified in a [dependency file](root:/saker.nest/doc/devguide/bundleformat.html#dependency-file):

```sakerscript
nest.dependency.resolve(
	DependencyFile: res/META-INF/nest/dependencies
)
```

In the above, the dependencies specified in the `res/META-INF/nest/dependencies` file will be resolved. The resolution works transitively. (I.e. the dependencies of the dependencies are also resolved, and so on.)

For example, if the file contains the following:

```plaintext
first.bundle
	classpath: 1.0
second.bundle
	native-lib: 2.0
```

The the dependencies for both `first.bundle` and `second.bundle` will be resolved. In order to filter out the unnecessary dependencies for your use-case, specify a dependency `Filter`:

```sakerscript
nest.dependency.resolve(
	DependencyFile: res/META-INF/nest/dependencies,
	Filter: nest.dependency.filter.kind(classpath)
)
```

In this case only the dependencies for the kind `classpath` will be resolved, and the dependency on `second.bundle` is omitted.

After resolving the dependencies, you can pass them to other tasks to consume. E.g. [](javacompileclasspath.md).