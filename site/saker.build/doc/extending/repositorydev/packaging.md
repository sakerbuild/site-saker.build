# Packaging

Your repository implementation should be distributed in a JAR file. The produced JAR should be self contained, meaning that all of the runtime classes should be bundled in it. It should not rely on manifest header like Class-Path and others. The build system will not take those into account when loading your classes.

Your package should contain a class file which is an implementation of [`SakerRepositoryFactory`](/javadoc/saker/build/runtime/repository/SakerRepositoryFactory.html). This class will be loaded first when the repository implementation is used.

Your archive may be [multi-release](https://openjdk.java.net/jeps/238), the build system will load the classes accordingly. However, module related information on JDK 9+ may not be taken into account.

Given a properly packaged archive, it can be used in the following way on the command line:

```
-repository pwd://myrepository.jar
-repository-id myrepo
-repository-class example.ExampleRepositoryFactory
```

The above example will load the repository factory with the class name `example.ExampleRepositoryFactory` from the archive located in the current process working directory with the name `myrepository.jar`. The repository will have the identifier `myrepo` during the build execution. See [](/doc/guide/repositories.md) for more information.

Note that the `-repository-class` option was necessary for the build system to locate the entry point for the repository. It can be omitted if the archive declares the [`saker.build.runtime.repository.SakerRepositoryFactory`](/javadoc/saker/build/runtime/repository/SakerRepositoryFactory.html) service using the [`ServiceLoader`](https://docs.oracle.com/javase/8/docs/api/java/util/ServiceLoader.html) facility of the Java runtime.\
This way, the `-repository-class` option may be omitted. 

<small>

Although declaring the service can be convenient, users should be noted that the `ServiceLoader` can find other classes too which are not present in the distributed JAR. This can happen in various cases such as when the user specifies a class path for the Java process that already has some repository service declaration. In order to promote determinism and reproducability, users are recommended to use the `-repository-class` option.

</small>
