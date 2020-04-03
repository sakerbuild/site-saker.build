# Overview

Saker.nest is the default package repository for the [saker.build system](root:/saker.build/index.html) providing access to build tasks, libraries, and other resources. The repository contains resource bundles enclosed in versioned package releases. Uploading and consuming packages from the repository is available to the general public through the saker.build system and via the [repository website](https://nest.saker.build).

## Features

* **Bundle storage.** The saker.nest repository generally serves as a resource bundle storage. The bundles have minimal format requirements, however, they may contain any kind of resources that make sense from the corresponding use-case perspective.
* **Immutability.** Any bundle that has been uploaded and published in the repository may not be modified later on. This ensures that bundle consumers won't have to deal with nondeterministic scenarios when working with a specific version of a bundle. \
Published bundles may not be removed from the repository by their uploaders, in order to prevent breaking dependent infrastructure. (However, bundles may be removed by administrators in case of violations.)
* **Dependencies.** Bundles may define arbitrary dependencies which can be interpreted by the consumers. The dependencies are declared in a meta-data file for a bundle, and aren't strictly formatted, therefore may be interpreted in an use-case dependent way. 

### Build features

The following features helps the integration with the saker.build system.

* **Build tasks.** Uploaded bundles may contain build tasks that can be used with the saker.build system. The repository maintains a task index that can be used to look up tasks based on their names. This allows the consumers to not query the repository API server for every task lookup, but perform it quickly on the local machine. (This also ensures consumer privacy in relation to the used tasks.)
* **Extensive storage configuration.** The repository used during build execution can be configured in various ways to properly manage the build task availability. The repository can be used in a completely offline way with local packages.
* **Script editor assistance.** Documentation can be attached alonside build tasks which are displayed in the build script editor if used within an IDE. (E.g. Eclipse) This helps writing build scripts without the need to frequently looking up information about the tasks in the browser.

### Java features

The following features help developing and consuming Java bundles from the repository. 

* **Classpath dependencies.** When a bundle's classpath is loaded, any dependencies declared with the `classpath` kind will be loaded for it automatically. This allows easy reuse of Java bundles in the saker.nest repository. 
* **Action support.** Bundles may be invoked using [repository actions](root:/saker.build/doc/guide/repositories.html#actions) in order to execute tasks outside of build executions. Actions can be used to develop programs that is easily distributed by the repository, and take advantage of its features.
* **Native library support.** Native libraries included in Java bundles are automatically loaded by the runtime. It also takes the underlying architecture and operating system type into account.  

## Getting started

The saker.nest repository is automatically included in the Saker.build system build executions (unless disabled). In order to download manually, or use a different version, see [](installation.md).
