# Basics

You can create Java archives using the [`saker.jar.create()`](/taskdoc/saker.jar.create.html) task with the [saker.build system](root:/saker.build/index.html). The task will create the archive based on the input parameters, and will put it in the build directory of the build execution.

The task is based on [`saker.zip.create()`](root:/saker.zip/taskdoc/saker.zip.create.html) and extends its functionality with additional parameters:

* Allows specifying manifest attributes.
* Allows specifying service declarations.
* Helps injecting main class and version attributes into the `module-info.class` entry.
* Allows specifying [multi-release](https://openjdk.java.net/jeps/238) contents.

## Output 

The output location of the task will be *always* in the build directory of the build execution. For the [`saker.jar.create()`](/taskdoc/saker.jar.create.html) task, it will be placed to the `{build-dir}/saker.jar.create/{Output-parameter}` location.

If you'd like to place it somewhere else than the build directory, we recommend that you copy it after the task completes.

The task ensures that an archive will be created only once for a given output location. Multiple archive creation tasks won't overwrite each others results.
