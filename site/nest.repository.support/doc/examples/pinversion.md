# Pin version

See also: [](../dependencyresolution/resolvingdependencies.md#version-pinning)

Pinning a dependency version may be important if you want to explicitly set the version of a resolved bundle. It is usually used for transitive dependencies. Version pinning works by explicitly adding a dependency on the bundle with a specific version:

```sakerscript
nest.dependency.resolve([
	example.bundle,
	# Pinning v1.0 of transitive dependency on depend.bundle
	depend.bundle-v1.0
])
```

If the dependencies of the `example.bundle` is the following:

```plaintext
depend.bundle
	classpath: [1.0, 3.0]
```

And you want to explicitly say that you want to use the `1.0` version of the `depend.bundle`, then explicitly adding `depend.bundle-v1.0` to the dependencies can be used to pin the bundle version.

The version pinning can also be used if you resolve the dependencies using a `DependencyFile`:

```sakerscript
nest.dependency.resolve(
	DependencyFile: res/META-INF/nest/dependencies,
	Bundles: [
		depend.bundle-v1.0
	]
)
```

Without the pinning, `depend.bundle` would resolve to the most recent suitable version. Note that a pinned bundle must satisfy the depedencies of others. In this case the pinned version must be in the range `[1.0, 3.0]`.