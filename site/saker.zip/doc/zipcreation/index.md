# Basics

You can create ZIP archives using the [`saker.zip.create()`](/taskdoc/saker.zip.create.html) task with the [saker.build system](root:/saker.build/index.html). The task will create the archive based on the input parameters, and will put it in the build directory of the build execution.

You can modify the archive creation process using the following ways:

* Adding files to the output archive. This can be done by either specifying their path, or based on a root directory from which the files should be added.
* Including files from other ZIP archives. This can be useful when creating archives that contain its dependencies.
* Using ZIP transformers to dynamically modify the archived contents. Transformers run as part of the archiving process and are presented an opportunity to modify the archive entries.

## Output

The output location of the task will be *always* in the build directory of the build execution. For the [`saker.zip.create()`](/taskdoc/saker.zip.create.html) task, it will be placed to the `{build-dir}/saker.zip.create/{Output-parameter}` location.

If you'd like to place it somewhere else than the build directory, we recommend that you copy it after the task completes.

The task ensures that a ZIP archive will be created only once for a given output location. Multiple ZIP creation tasks won't overwrite each others results.
