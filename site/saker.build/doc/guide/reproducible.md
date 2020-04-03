# Reproducible builds

The definition of reproducible builds is the following according to [reproducible-builds.org](https://reproducible-builds.org/docs/definition/):

> A build is reproducible if given the same source code, build environment and build instructions, any party can recreate bit-by-bit identical copies of all specified artifacts.

Saker.build aims to allow reproducible builds, but it is not only the responsibility of the build system, but as well as the developers and build task implementers. In this section we'll discuss how saker.build relates to reproducible builds, and what the steps developers should take to help that.

## Saker.build reproducability

Saker.build aims to limit the scope of the build environment. When the same builds are executed on different machines, the contents on the build machine or process environment should not affect the configuration of the build. Saker.build uses no properties files in known locations to load configuration related data. Doing so could make the builds different when executed on semantically the same machines. Saker.build also defines no environment variables or Java system properties that would modify the build configuration.

All of the build related configurations should be on the command line of the build invocation. (Or in other related configuration locations in case the build system is invoked differently, e.g. IDE.) This behaviour makes easier to configure the builds exactly the same way on different machines.

Saker.build defines a side-effect free build task model that encourages separation of concerns for their executions, but it doesn't actually sandbox their invocation for performance reasons. It is the responsibility of the developer to make the tasks operate on separate data. The data based dependency model encourages that the tasks are invoked in the appropriate order.

The build system also has no notion of time related file attributes available for the tasks. The build system maintains its own file hierarchy for build executions and the tasks which derive their contents based on files have no way of accessing any of the last modification time, last access time, or creation time file attributes. The lack of this functionality disallows tasks to create different results based on these file attributes, which can help the build results to be identical bit by bit. (Of course, some exceptions may apply e.g. when using external processes.) 

## Recommendations

The build system won't make your build automatically reproducible. You, as a developer need to actively write the build scripts that ensure this and use tasks that provide such guarantee.

As the first recommendation, you should define your build tasks according to the execution model of the SakerScript language. When a build task of yours use the result of another task, then the input should be explicitly present as a parameter of the build task that uses it. The tasks shouldn't communicate through shared environment resources without their relation being present in the build script.

Secondly, your tasks should have separate output locations. Don't configure tasks in a way that results in a file being written twice during build execution. It may not cause trouble, but can result in ordering issues and race conditions when other tasks access the overwritten file.

As part of the previous point, we recommend that all the outputs of your tasks should reside under the specified build directory. The ordering of the tasks should be explicitly present similar to the following way:

```sakerscript
$compileresult = example.compile.sources()
example.create.package($compileresult)
```

This ensures that the `example.create.package()` task will use the results of the compilation after it has been completed, and no race conditions occur between writing the outputs and reading it.

When multiple versions of some resource (e.g. build task, libraries, etc...) is available, developers are recommended to choose a fixed version in some way. This ensures that always the same steps are run every time the build is invoked and automatic updates won't distrupt the results.

All of the user generated resources that are part of the build inputs should be present as part of the redistributed sources. It is acceptable to have a dependency on resources in publicly accessible repositories, but the build should not depend on any sources that are not uploaded to source control. The simplest way to achieve this is to only access resources under the working directory of the project for executing the build.
