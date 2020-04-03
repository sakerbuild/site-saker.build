# Build system runtime

The saker.build system uses the Java Virtual Machine (JVM) to run. It was chosen to be the virtual machine under the build system as it provides excellent cross-platform support and dynamic code loading capabilities. This is the only and single dependency that you need to run it.

The build system lives in a single JAR (Java ARchive), which consists of the minimal necessary code to execute a build. Apart from the [built-in tasks](/doc/scripting/builtintasks/index.md), it cannot do anything on its own. The built-in tasks themselves are not sufficient to properly build a software project.

The build system requires plugins which are able to do the actual work necessary for software building. These plugins are the tasks that you call from the build script. The tasks can be provided by repository implementations, which are responsible for looking up the tasks for a given name.

Saker.build uses the [saker.nest repository](root:/saker.nest/index.html) as its default repository for looking up tasks. When you run a build without any specific configuration, this repository is used to look up the tasks that you declare in the build scripts. The repository is a public database of tasks and other bundles which can be used to build software. See the [saker.nest repository website](https://nest.saker.build) for more information. See [](/doc/extending/repositorydev/index.md) for developing your own repository.

The build system also comes with a [built-in language](/doc/scripting/index.md) (SakerScript), that is included in the JAR distribution. It is the language that you saw in the previous chapters of the user guide. Like repositories, other languages can also be used if one chooses to [implement one](/doc/extending/scriptdev/index.md).

Both the repositories and scripting languages are loaded dynamically by the build system. The dynamic loading enables other features such as build clusters or repository actions to work properly.

The build system uses a specific [storage directory](envconfig.md#storage-directory) to store information that is unrelated to built projects, but are necessary for the operation of other features. In this storage directory, the build system loads dynamic class paths, and the loaded repositories can use it as storage for associated data. The usage of the storage directory may be extended in the future.
