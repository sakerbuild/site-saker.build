# Task repositories

Task repositories are the primary extension points for the build system. They provide the lookup functionality for the named tasks in the build scripts. Each repository is loaded dynamically by the build system and manages their lifecycle accordingly.

Repositories can use their own storage directories under the build environment [storage directory](envconfig.md#storage-directory) and can store various data there. It is primarily intended to be a shared storage for a given repository to share data between different projects and cache repository related data.

For saker.build, we've developed our own public repository that is able to host and look up tasks and their related implementation. See the [saker.nest repository](root:/saker.nest/index.html) for more information. \
The saker.nest repository is automatically included in the build configuration if not specified otherwise. (It will have the `nest` identifier by default.)

When a repository is configured for the build, it will have an user-specified or generated identifier. This identifier can be used to reference the loaded repository when necessary. One example for this is when developers want to explicitly specify the location from where a given task should be loaded:

```sakerscript
# the following task will be loaded from 
#   the repository with 'nest' identifier 
example.compile.sources@nest()
# the following task is loaded from the first
#   repository that can provide a task with that name
example.compile.sources()
```

## Actions

Repositories can optionally implement the functionality for executing custom actions. Actions are basically the `main` functions of a repository. A repository action is executed with a list of string arguments, and the operations that they do can be completely arbitrary.

The repository actions can be invoked via the command line [](cmdlineref/action.md) command.

Make sure to read the documentations for a given repository when invoking actions of theirs.

## Configuration

The repositories can be configured by specifying a class path for them. The repository identifier can also be specified, but if they are omitted, one will be automatically generated for them.

An example for specifying a custom repository using the command line arguments:

```
-repository pwd://myrepo.jar
```

The task repository from the `myrepo.jar` in the current process working directory (pwd) will be loaded for the build execution. The actual repository class is determined using the Java [`ServiceLoader`](https://docs.oracle.com/javase/8/docs/api/java/util/ServiceLoader.html) mechanism. An implementation dependent identifier is generated for it.

The configuration also implicitly includes the Nest repository.

---

```
-repository pwd://myrepo.jar
-repository pwd://secondrepo.jar
-repository-id secondrepo
-repository-class secondrepo.SecondRepository
-repository-no-nest
```

In the above example we specify the `myrepo.jar` similar to the previous example, but also add another repository from the `second.jar` JAR archive with the identifier of `secondrepo`. The `secondrepo.SecondRepository` class will be loaded from the archive to load the repository.

With the [`-repository-no-nest`](/doc/guide/cmdlineref/build.md#-repository-no-nest) flag we specify that the Nest repository shouldn't be used in this build execution.

See the [command line reference](cmdlineref/build.md) for more information.