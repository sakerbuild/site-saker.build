# Local bundles

The [local storage](localstorage.md) allows developers to store bundles and packages on their local machine. These bundles are accessible to all other build tasks running on the same machine.

The local storage serves as a local repository of bundles that can be shared by different projects that are being developed on the same machine. An use case for this is to make available the build result of a project and be consumed by other project that are being developed.

The local storage also allows build tasks to be loaded by the build executions and can be used to easily develop build tasks and automatically load them in the builds.

In order to take advantage of the local bundle storage, you need to install bundles to it.

## Installing bundles

Installing a bundle to the local bundle storage basically means that putting them to a shared location on the local machine. The other agents can then consume these bundles as they like.

The installation of bundles can be done during build execution, or via repository actions.

#### Build execution

You can use the [`nest.local.install()`](root:/nest.repository.support/taskdoc/nest.local.install.html) build task to install a bundle automatically when you build it.

See the [](root:/nest.repository.support/doc/examples/localinstall.html) example for more information.

#### Repository action

The [`local install`](/doc/userguide/cmdlineref/local_install.md) repository action can be used to install the bundle to a given local bundle storage.

This can be used if you simply want to install some already built bundles. An example for this:

```plaintext
java -jar saker.build.jar action local install bundles/*.jar
```

The above will install all `.jar` bundles in the `bundles` directory relative to the process working directory.
