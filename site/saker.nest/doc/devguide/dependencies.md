# Dependencies

We've [previously seen](bundleformat.md#dependency-file) how dependencies can be declared in a bundle. But what are they actually?

Dependencies specify the relation of bundles that they need for performing a given operation. The dependencies are purposefully made to be generic declarations rather than specifying them for a single use-case. In the most commonly used scenario, dependencies define how the classpath for a given bundle should be loaded. However, they also enable other interpretations of the dependency declarations.

A dependency on a bundle specifies a requirement relation for the subject bundle of the dependency. If the bundle `my.bundle` declares a dependency on another bundle `other.bundle`, then it is to be assumed that for an operation to be executed with `my.bundle`, the `other.bundle` may also be necessary for performing it.

## Dependency kinds

Dependency kinds specify the scenario in which the dependency should be used. The most common is `classpath` in which case a dependency is used when the classpath for a bundle is being loaded.

Dependency kinds allows separation of different types of dependencies when a bundle serves multiple purposes. For example if a bundle contains Java classpath files to be used during compilation, and native libraries to be linked alongside with the bundle, then declaring dependencies with different kinds can be used to resolve different bundle set for the given operation:

```plaintext
library.bundle
	compile-classpath: 1.0
native.library.bundle
	link-library: 1.1
```

In the above (completely hypothetical) example, if we'd like to resolve the bundle set that is required for Java compilation, we'd resolve the dependencies for the `compile-classpath` kind. In this case, any dependencies that don't have this kind will be omitted. \
If we want to get the bundles that should be used for native library linking, we would use the `link-library` dependency kind to get the bundle set to be used. \
We can see that dependency kinds helps the separation of use-cases for multi-content bundles.

The dependency kinds may be arbitrary strings (matching the `[a-zA-Z_\-0-9]+` regular expression) based on the use-case scenario. The most commonly used is `classpath` that specifies the required bundles to be loaded alongside the bundle classes. See [](classpath.md) for more information.

## Dependency meta-data

The dependency declarations allow arbitrary information to be added alongside them. Meta-data can be used to control the interpretation of the dependency for a given kind. This interpretation is the responsibility of the consumer of the dependency.

The following meta-data names are recognized by the repository runtime and used when generally resolving dependencies:

* `optional`: The value of the meta-data is interpreted as a boolean, signaling whether a given dependency is required or not. This dependency meta-data can be used on **all** dependencies, regardless of kind. \
If a dependency is declared to be optional, the dependency resolution algorithm will not fail if the dependency cannot be satisfied. The optional dependencies will be only part of the dependency resolution result set if it can be satisfied without conflicts alongside the required dependencies.

The following meta-data names are applied when [dependency constraints](../userguide/constraints.md) are used:

* `jre-version`: Specifies a Java major [version range](../userguide/versioning.md#version-ranges) that this dependency applies to. If the current JRE major version is not in the range of the specified value, then the dependency will be omitted. \
E.g. This meta-data name can be used to depend on different bundles on different Java versions:
	```plaintext
	bundle.for.jdk8
	    classpath: 1.0
	        jre-version: 8
	bundle.for.jdk9
	    classpath: 1.0
	        jre-version: 9
	```
* `repo-version`: Specifies a repository [version range](../userguide/versioning.md#version-ranges) that this dependency applies to. If the current repository runtime version is not in the range of the specified value, the the dependency will be omitted.
* `buildsystem-version`: Specifies a saker.build system [version range](../userguide/versioning.md#version-ranges) that this dependency applies to. If the current build system version is not in the range of the specified value, the the dependency will be omitted.
* `native-architecture`: Specifies a comma separated aarchitecture list that contains the native architectures the dependency applies to. If the current architecture constraint is not included in the specified list, the associated dependency will be omitted. \
E.g. This meta-data can be used to depend on different bundles for different architectures:
	```plaintext
	bundle.for.x86
	    classpath: 1.0
	        native-architecture: x86
	bundle.for.x64
	    classpath: 1.0
	        native-architecture: amd64
	```

Note that the above constraint based meta-data may not be taken into account for all dependency resolution use-cases. They will be taken into account when loading the classpath of the bundles.
