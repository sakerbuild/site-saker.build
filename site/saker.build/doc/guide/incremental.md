# Incremental builds

Incremental builds mean that when a build is executed, the results of the previous build may be reused. This can greatly reduce the build times in the edit-build-debug work cycle of the developer. Saker.build aims to provide the best possible solution for incremental builds by providing a rich API for the task implementations.

As a user, this should be transparent to you, and you shouldn't have to deal with this when writing build scripts. Let's look at an example of an incremental build:

```sakerscript
example.compile.sources(*.cpp)
```

The above task compiles all of the `cpp` files in the current working directory. When you run your build in a clean state, all the `cpp` files will be compiled. When you run your build the second time, without any modifications to the `cpp` files, the build system will determine that no files have been changed, therefore skip the `example.compile.sources()` task call. The second build will finish much quicker as the build system correctly detected that no changes have been made, and the results of the compilation is up to date.

When you modify a `cpp` file, the task will be called, and it should only compile the `cpp` file that was modified, *but not all of them*. If you have several hundred or thousand files, and only modify one, only the modified one is recompiled, not all of them. This spares a great amount of time in your development cycle.

When you remove a `cpp` file, the task will be invoked again, and it should detect that a source file was removed, therefore deleting the previous output, quickly.

When you add a new `cpp` file in the working directory, the task will be called again. It is notified about the file addition, and only the newly added file will be compiled.

Note that you don't need to explicitly specify all of the source files you have, and can often use wildcards to configure the compilation. Saker.build was designed to allow wildcard and other file discovery mechanisms from the start, to make the configuration more convenient.

Saker.build supports the task incrementality via a rich API for the tasks, that allows the lowest level of dependency management. This results in only running the tasks that must be run, and skipping the ones which are up to date. See [](/doc/extending/taskdev/incrementaldeltas.md).
