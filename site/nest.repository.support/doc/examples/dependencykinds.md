# Dependency kinds

Bundles are allowed to define various dependencies with different kinds. The kinds are used to differentiate the dependencies based on their use-case. The kind dependency filter can be used to only resolve dependencies which have any of the specified kinds:

```sakerscript
nest.dependency.resolve(
	DependencyFile: res/META-INF/nest/dependencies,
	Filters: nest.dependency.filter.kind(classpath)
)
```


If the `res/META-INF/nest/dependencies` contains the following contents:

```plaintext
example.bundle-api
	classpath: [1.0)
library.bundle
	native-lib: [2.3)
```

Then specifying the above kind dependency filter will result in only the `example.bundle-api` being resolved. The dependency with any other kind(s) are omitted.

The [`nest.dependency.filter.kind()`](/taskdoc/nest.dependency.filter.kind.html) task can take one or more dependency kinds to filter the interested dependencies.

```sakerscript
nest.dependency.resolve(
	DependencyFile: res/META-INF/nest/dependencies,
	Filters: nest.dependency.filter.kind([
		classpath,
		native-lib
	])
)
```

If we specify the above, then no dependencies will be omitted from the dependency file. However, any other kinds from transitive dependencies will be omitted accordingly.
