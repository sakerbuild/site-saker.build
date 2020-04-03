# Compile filter

The compile dependency filter can be used to omit non `compile-transitive` dependencies. It can be useful to limit the dependencies when only compiling against an API of a package.

The following example is taken from [](../dependencyresolution/filters.md#compile-filter):

Let us work with the following bundles, and their dependencies:

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

We have a separated package taking the [recommendations](root:/saker.nest/doc/devguide/dependencyhell.html) for avoiding dependency hell into account.

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

The result of the dependency resolution will be the following:

```plaintext
example.bundle-api-v1.0
```

The `example.bundle-impl` is not part of the result, as it is omitted by the filter, due to the `compile-transitive: false` being set on the dependency.

If we depend on `example.bundle` instead of `example.bundle-api`:

```sakerscript
nest.dependency.resolve(
	Bundles: [
		example.bundle
	],
	Filters: nest.dependency.filter.compile(CompileTransitive: false)
)
```

The result of the dependency resolution will be the following:

```plaintext
example.bundle-v1.0
example.bundle-api-v1.0
```

The `impl` bundle is still not part of the results.

After resolving the dependencies, you can pass them to other tasks to consume. E.g. [](javacompileclasspath.md).