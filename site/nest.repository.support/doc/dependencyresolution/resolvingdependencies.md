# Resolving dependencies

The [`nest.dependency.resolve()`](/taskdoc/nest.dependency.resolve.html) task can be used to resolve dependencies in the saker.nest repository. The specified dependencies are resolved using the alroithm specified by the repository.

The task can resolve explicity specified dependencies, as well as dependencies specified using a [dependency file](root:/saker.nest/doc/devguide/bundleformat.html#dependency-file). The nature of the dependency resolution can be customized using the `Filters` parameter, which can be used to set filters for the dependency resolution. The filters are used to specify which dependencies should be resolved, and which ones should be omitted.

The task can also be used to resolve the versions of bundles without taking the transitive dependencies into account.

Note that in the following examples all of the dependencies for the associated bundles are resolved. In order to resolve dependencies for a specific kind, see [](filters.md).

## Resolve bundles

Simple bundle dependencies can be resolved in the following way:

```sakerscript
nest.dependency.resolve([
	first.bundle,
	second.bundle-api,
	third.bundle-v1.0
])
```

In the above, we resolve the dependencies for the specified bundles. The given bundle identifiers may contain qualifiers, and version numbers as well. If the bundle has no version number defined for them, then it will be determined by the dependency resolution algorithm (usually the latest release).

## Dependency file

The task can accept a [dependency file](root:/saker.nest/doc/devguide/bundleformat.html#dependency-file) that declares the dependencies to be resolved:

```sakerscript
nest.dependency.resolve(
	DependencyFile: res/META-INF/nest/dependencies
)
```

In the above, the specified file will be parsed by the task, and the declared dependencies in it will be resolved.

### `this`

The dependency file allows the `this` identifier to be specified in which case the version of the enclosing bundle will be substituted in place of it. The `SelfBundle` parameter allows specifying the bundle that should be considered to be enclosing the given `DependencyFile`:

```sakerscript
nest.dependency.resolve(
	DependencyFile: res/META-INF/nest/dependencies,
	SelfBundle: example.bundle-v1.0
)
```

In this case, if the specified dependency file contains a `this` identifier, the version `1.0` is substituted in place of it.

**Note** that if the dependency file contains a `this` token, and a self bundle is not specified, an exception will be thrown, as the dependency file couldn't be parsed.

## Version pinning

When using dependency files, or dealing with transitive dependencies, you may need to pin specific bundle versions. You can do that by specifying bundles with versions alongside the dependency file:

```sakerscript
nest.dependency.resolve(
	DependencyFile: res/META-INF/nest/dependencies,
	Bundles: [
		first.bundle-v1.0,
		second.bundle-v2.0
	]
)
```

The above adds the `first.bundle-v1.0` and `second.bundle-v2.0` dependencies to be resolved alongside the dependency file. Specifying the bundles with a given version will result in them being used rather than choosing an other one with possibly different version.

**Note** that this is not actually version pinning, but adding additional dependencies with an exact version. If the dependency file doesn't declare dependency on the given bundles (directly or transitively), the specified bundles will still be resolved.

## Dependency kinds and filtering

Please see [](filters.md).

## Dependency constraints

The [`nest.dependency.resolve()`](/taskdoc/nest.dependency.resolve.html) task allows resolving the dependencies with a different set of [dependency constraints](root:/saker.nest/doc/userguide/constraints.html). You can use the `DependencyConstraints` parameter so specify different constraints for the dependencies:

```sakerscript
nest.dependency.resolve(
	DependencyConstraints: {
		JREMajorVersion: 9
	}
)
```

In the above, the dependencies will be resolved as if they were constrained for a Java runtime with major version of 9.

See [](root:/saker.nest/doc/userguide/constraints.html) for more information about dependency constraints.
