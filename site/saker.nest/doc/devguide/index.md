# Overview

The following sections and documents serve as a guide for developing packages for the saker.nest repository. The documents focus on integrating with the repository rather than with the saker.build system. For build task development see the [task development guide](root:/saker.build/doc/extending/taskdev/index.html).

When developing bundles, you need to adhere to the expected [bundle format](bundleformat.md) that the created JAR archives need to conform to. Saker.nest will perform basic validations when a bundle is loaded in the runtime. Loading a bundle may not necessarily mean that the Java classes from it are loaded, but only that it is opened by the runtime.

The Java class loading will be done based on the `.class` file contents of a bundle and the declared `classpath` dependencies. The class loading also takes the [dependency constraints](../userguide/constraints.md) into account.

If your bundle declares [build tasks](tasks.md), then they will be loaded automatically when a build script references them.

During development, you can write integration tests, as well as test your build tasks and bundles by using the [local](/doc/userguide/localstorage.md) or [parameter](/doc/userguide/paramsstorage.md) bundle storages.

