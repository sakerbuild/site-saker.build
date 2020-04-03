# Overview

The [saker.jar](https://nest.saker.build/package/saker.jar) package in the [saker.nest repository](root:/saker.nest/index.html) provides features for [Java ARchive](https://en.wikipedia.org/wiki/JAR_(file_format)) creation using the [saker.build system](root:/saker.build/index.html).

The [`saker.jar.create()`](/taskdoc/saker.jar.create.html) task allows you to define the contents of the created archive while also providing features for specifying Java related contents. The task is based on the [`saker.zip.create()`](root:/saker.zip/taskdoc/saker.zip.create.html) task, and provides additional features for it.

The task allows you to specify manifest attributes, services, multi-release contents, and allows injecting module-info related meta-data.
