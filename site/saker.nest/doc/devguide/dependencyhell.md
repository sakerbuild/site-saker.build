# Dependency hell

[Dependency hell](https://en.wikipedia.org/wiki/Dependency_hell) is the scenario when multiple versions of a given package is present in a system and relationships between the packages cause different versions of a package to be loaded. This can cause compatibility issues between packages as the semantically same classes may be loaded multiple time by the runtime therefore making them incompatible.

Saker.nest doesn't attempt to provide a direct solution to this problem, but presents development guidelines that help avoiding these issues. As a baseline, see the issue example in presented in the [](classpath.md#multiple-classloaders) section.  

## Separation of concerns

A solution for the mentioned problem is by separating the concerns when developing bundles. When creating a package, we recommend separating the bundle classes in the following way:

* **API bundle**: This bundle contains the classes that other packages can depend on. This contains the classes that provide access to the functionality provided by the package. Generally, this contains classes and interfaces that have a stable binary interface that other bundles can use.
* **Implementation bundle**: Contains the implementational details of a given package. Implementational details shouldn't leak into the API bundle, and classes in this bundle shouldn't be used directly by external agents. The binary interface of the classes in this bundle is not required to be stable.
* **Main bundle**: Contains implementational classes similar to the implementation bundle, but they are responsible for providing a fronted for accessing via build scripts. This bundle should contain the build task classes that are loaded during build execution. The classes from the main bundle shouldn't escape the main bundle domain. 

Based on the above, let's see how the dependencies are between these bundles:

```plaintext
example.bundle
        |
        | [0)
        |
        V
example.bundle-api
    |          /\
    | [this]    |
    |           | [this]
    V           |
example.bundle-impl
```

The point of this architecture is to have the classes that are interchangeable by different packages to be only loaded **once** by the repository runtime. The dependency schema as above will solve this problem, as the `[0)` dependency version range ensures that only the most recent version of the `-api` bundle is loaded.

The `[0)` version range stands for *minimum version of 0, with unbounded maximum*. This basically specifies no restriction on the used version of the dependent bundle. When depending on `-api` bundles, we strongly recommend using only the `[0)` version range for the dependency, as this way the loaded bundles can be shared by different packages.

The `[this]` range specifies that the `-api` bundle and `-impl` bundle dependencies should be resolved from the same package version release as they are in. This ensures that there won't be binary incompatibility between the `-api` and `-impl` bundles.

Converting the above dependency schema into dependency file format:

*example.bundle*:
```plaintext
example.bundle-api
    classpath: [0)
```

*example.bundle-api*:
```plaintext
example.bundle-impl
    classpath: [this]
```

*example.bundle-impl*:
```plaintext
example.bundle-api
    classpath: [this]
```

#### Restrictions

The proposed development practice comes with the following restrictions.

**Use the a version range that doesn't have an upper bound when depending on `-api` bundles.** This is the most important restriction that is the base of the proposed solution. Using version ranges that have no upper bounds will always cause the most recent release of the bundle to be loaded, therefore avoiding multiple class loader problems. The `[0)` dependency version range as suggested in the above example is the recommended version range when depending on `-api` bundles.

**Don't make incompatible changes in the `-api` bundle.** Based on the above, we can say that always the most recent version of the `-api` and `-impl` bundles will be loaded in the runtime. This means that both the oldest and newest releases of the consumers must work with the package. You cannot just simply remove classes, interfaces, methods, and fields from the `-api` bundle, as that could break consumer package functionality.

We recommend that you gradually deprecate features in your packages over time in a timely manner that makes sense from consumer perspective.

**Don't depend directly on `-impl` bundles.** Implementation bundles may be subject to breaking changes without notice. Depending on them may cause your package to break when the dependent package is updated.

**Don't depend on the main bundle from `-api` or `-impl`.** The dependency from the main bundle should be one way to the `-api` bundle, and the `-api` bundle mustn't depend on the main bundle. If the `-api` bundle depended on the main bundle, that would risk the `-api` bundle being loaded multiple times, which is what we're trying to avoid. 

#### Notes & exceptions

The following notes and exceptions apply when employing the above development practice.

**You can specify bounded dependency on the main bundle.** You can depend on the main bundle of a package in a version bounded way. As depending on it won't change the way the `-api` bundles are loaded, it makes no difference from class sharing perspective..

**Classes from the main bundle shouldn't be shared.** If you use a class from the main bundle of a package, then that class shouldn't be passed to other bundles for use. The classes in the main bundle serve the purpose of transforming the user input into classes from the `-api` and `-impl` bundles. You can use the classes from a main bundle, but they shouldn't be passed along the processing pipeline.

**You don't always need to break up your bundles.** If you don't intend for others to directly use the classes in your bundle, then you don't need to employ the separation of concerns. In this cases, your classes may be loaded multiple times by the repository runtime, however, it won't be a problem, as the class conflicts can't occur, because others aren't depending on your bundle.

**You don't always need an `-impl` bundle.** If your use-case don't include others instantiating your implementational classes, you can avoid the need to create an `-impl` bundle. This can be a scenario when others would want to consume the output of your build task, but don't want to invoke the task itself in a programatic manner. In this case you don't need to separate the implementation to an `-impl` bundle, but only need to have the task output implement an appropriate interface from the `-api` bundle.

**For simple use-cases, you don't even need an `-api` bundle.** If you don't want others to interact with your classes in any way, you don't need to separate the bundles. However, note that if you plan on using the output of your build task as an input to another, then you are **strongly** recommended to create an `-api` bundle. Not doing so may cause incompatibility between your own tasks. \
For example if your bundle contains build tasks with the names `first.task` and `second.task`:

```sakerscript
$firstout = first.task-v1.0()
second.task-v2.0($firstout)
```

The above will most likely cause incompatibility if you don't have an `-api` bundle. The output of the `first.task` should implement an interface from the `-api` bundle, that the `second.task` should use to interpret its input.

## Native libraries

If your bundle includes native libraries, you must develop your bundle in a very specific way. This is due to the fact that the JVM **can't load a native library multiple times.** If there can be multiple instances of your bundle classloader (as seen in [](classpath.md#multiple-classloaders)) then the library loading for the second classloader will *fail*. This can cause unexpected and hard to solve issues in the runtime.

The solution for this is that **a bundle that loads native libraries must not have any dependencies**. If your bundle doesn't have any dependencies, then the runtime will **never** construct a second classloader for your bundle. The bundle should have only one single purpose, that is providing access to the native library functionality.

Any operations that deal with the native library, and use third party dependencies should be exported to another bundle, like the following dependency graph:

```plaintext
example.bundle------>dependency.bundle
      |
      |
      V
example.bundle-native
    (no dependencies at all)
```

In the above example, the `example.bundle` depends on the `example.bundle-native` that contains the native library to be loaded. The `-native` bundle provides access to the functionality of the native library, and the lack of dependencies of it ensures that it will be loaded at most once.

Any operations and resource management should be present in the `example.bundle`. It may also depend on other packages, but the `-native` bundle must not.

*Note*: Different versions of a bundle that contains native libraries may be loaded by the repository runtime. This is an acceptable scenario, unlike the case with `-api` bundles. You are not required to use an unbounded version dependency on native bundles.
