# Dependency filters

Dependency filters are the main way of configuring how the dependencies are resolved by the [`nest.dependency.resolve()`](/taskdoc/nest.dependency.resolve.html) task. A dependency filter specifies a mechanism of which and how the given dependencies should be treated. They also get the opportunity to filter transitive dependencies.

The dependency filters can be applied using the `Filters` input parameter:

```sakerscript
nest.dependency.resolve(
	Filters: nest.dependency.filter.kind(classpath)
)
```

The above will resolve only the dependencies that are specified with the kind `classpath`.

## Kind filter

The [`nest.dependency.filter.kind()`](/taskdoc/nest.dependency.filter.kind.html) task can be used to specify a dependency filter that limits the resolved dependencies to those that are declared with the specified kinds:

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

Specifying a kind dependency filter can be useful when the dependencies are resolved and to be used for a given use-case.

(Note that the `native-lib` dependency kind in the above example is fictional, and for exemplary purposes.)

## Compile filter

The [`nest.dependency.filter.compile()`](/taskdoc/nest.dependency.filter.compile.html) task creates a dependency filter that works similarly to the [kind filter](#kind-filter), but handles the `compile-transitive` meta-data on the dependencies. The advantage of the compile filter is that it can avoid pulling in unnecessary dependencies.

For the following example, let us work with the following bundles, and their dependencies:

Bundle `example.bundle-v1.0`:

```plaintext
example.bundle-api
	classpath: [0)
```

Bundle `example.bundle-api-v1.0`:

```plaintext
example.bundle-impl
	classpath: [this]
		compile-transitive: false
```

Bundle `example.bundle-impl-v1.0`:

```plaintext
example.bundle-api
	classpath: [this]
```

You can see that we have a separated package taking the [recommendations](root:/saker.nest/doc/devguide/dependencyhell.html) for avoiding dependency hell into account.

The use-case for the compile filter is to resolve the bundle dependencies that are required for compiling against a given bundle, but omitting the transitive dependencies which are implementational details.

We can use it the following way:

```sakerscript
nest.dependency.resolve(
	Bundles: [
		example.bundle-api
	],
	Filters: nest.dependency.filter.compile(CompileTransitive: false)
)
```

The above dependency resolution will only result in `example.bundle-api-v1.0`, as the `example.bundle-impl` transitive dependency is omitted, because it is marked with the `compile-transitive: false` meta-data. The compile filter will omit that dependency.

If we set the transitivity parameter to `true`:

```sakerscript
nest.dependency.resolve(
	Bundles: [
		example.bundle-api
	],
	Filters: nest.dependency.filter.compile(CompileTransitive: true)
)
```

Then the result bundle dependencies will be:

```plaintext
example.bundle-api-v1.0
example.bundle-impl-v1.0
```

Note that the [`nest.dependency.filter.compile()`](/taskdoc/nest.dependency.filter.compile.html) task can also take dependency kinds as input similarly to [`nest.dependency.filter.kind()`](/taskdoc/nest.dependency.filter.kind.html). The task can work with other kind of dependencies based on your use-case.

Note that in order to avoid accidental errors, the default value for the `CompileTransitive` parameter is `true`. You must explicitly set it to `false` in order to omit `compile-transitive` dependencies.

