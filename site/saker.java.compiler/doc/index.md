# Overview

The [saker.java.compiler](https://nest.saker.build/package/saker.java.compiler) package in the [saker.nest repository](root:/saker.nest/index.html) provides features for Java compilation using the [saker.build system](root:/saker.build/index.html). As its main feature, it supports incremental compilation, meaning that for small modifications of the codebase, it will perform the minimal required recompilation of your project.

The saker.java.compiler package supports scalable incremental Java compilation outside and inside of an IDE, including support for annotation processors. It is the *single, correct and only complete solution for incremental Java compilation* that is currently available in the software development industry. (See [](featurecomparison.md))

Use the [`saker.java.compile()`](/taskdoc/saker.java.compile.html) task with the saker.build system to compile your Java projects. The simplest example for getting started is:

```sakerscript
saker.java.compile(src)
```

The above will compile the Java sources contained in the `src` subdirectory of the working directory. For further examples see [](gettingstarted.md) and [Examples](examples/index.md).

If you're interested in how the performance of the  [`saker.java.compile()`](/taskdoc/saker.java.compile.html) task compares to other build systems, see the [](performancecomparison.md).
