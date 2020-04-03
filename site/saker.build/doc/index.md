# Overview

Saker.build is an all-in one, universal build system that focuses on incremental and scalable builds. It natively supports all core features that can be expected from a modern build system and lets you work with projects of all sizes. It supports incremental builds down to its core making it a perfect candidate for day-to-day use.

## Features

Some of the features that saker.build includes:

* **Extensibility.** Be it custom tasks, new build script languages, or custom task management implementation, saker.build lets you extend it in various ways. It provides a rich API to take full advantage of the build system services.
* **Incrementality.** Saker.build considers incremental builds to be the highest priority. It strives to rebuild only parts of the project which have changed and is built from the ground up to support this facility. Short incremental builds lets you think more about your code rather than waiting for the project to compile.
* **Scalability.** Saker.build is able to scale both vertically (faster CPU) and horizontally (more build PCs). Maximizing parallelization of build tasks and the support for build clusters bring great performance for both small and large projects.
* **Build clusters.** You don't need to install any other tools to use multiple PCs for building your project. Support for dispatching build tasks over the network is included.
* **Build language.** Saker.build uses a custom language for the build definitions by default. The language focuses on performance and readability while using a clutter-free syntax.
* **Build cache.** (Work in progress) If a task has already been run by others, the build cache lets you download its outputs rather than executing it again on your machine.

##### Other features

Some features which are not strictly part of the saker.build system, but still important to mention:

* **Truly incremental Java builds.** Outside of an IDE, incremental Java compilation fails short. The dependency analysis and annotation processor handling is not a trivial task. However, we've implemented just that, being the first build system to support fully incremental Java builds. See [saker.java.compiler package](root:/saker.java.compiler/index.html) and it's [feature comparison](root:/saker.java.compiler/doc/featurecomparison.html) for more information.
* **Incremental Java testing.** Testing is a crucial part of software development. However, running all the tests for little changes in your code can be extremely time consuming. The [`saker.java.test()`](root:/saker.java.testing/taskdoc/saker.java.test.html) task implements incremental testing functionality which supports the reinvocation of test only if their dependencies have changed. See [saker.java.testing](root:/saker.java.testing/index.html) for more information.
* **IDE plugin support.** The build system can integrate well with IDEs. It can configure the projects according to the build scripts, and provide rich scripting support for build script editing. Content assistant features, documentation lookup, and script outline can greatly help writing build scripts for saker.build. See [IDE plugin installation guide](installation.md#ides) for more information.
* **Nest repository.** The default task repository for saker.build is the [Nest repository](root:/saker.nest/index.html). It is a public database of build tasks where everyone can publish their implementations. Using it can be greatly beneficial to the community and developers can quickly add support for other languages.

## Adopting saker.build

As of writing this document, saker.build is still a relatively new technology (some might say [bleeding edge](https://en.wikipedia.org/wiki/Bleeding_edge_technology)). It promises improvements over using other build system, while also having the risk of encountering bugs and lack of features more frequently than an established tool. We welcome everyone who decides to use saker.build as their project build tool, however, we also recommend gradual adoption of it:

**You should gradually migrate the build process of your software to saker.build.**

This means that you can add saker.build support for separate components of your software, while not getting rid of the build tool that you've been using previously. At the beginning, you'll be maintaining the build scripts for two build systems. This has the advantage of falling back to the original one if you encounter a blocking bug with saker.build. This methodology has the advantage that you can use saker.build for daily development, benefiting from the performance increase of saker.build.

*But then do I need to modify two build scripts if I add a new source file?* You might ask. Generally, no. Saker.build supports file wildcards, meaning that you can define your build scripts so that they automatically detect new file additions and recompile the relevant sources. You'll most likely only incur the cost of maintaining two build systems when you actually modify the runtime aspects of a build process.

**Benefits.** Apart from having a [better performing](/doc/perfcomparison.md) build process, adopting saker.build at this stage of the development allows your feedback to have a greater impact on the further development of it. If you encounter any difficulties, bugs or inconveniences, filing an issue and providing feedback can help us improve on it in a way that benefits everyone.

See [](installation.md) to get started.
