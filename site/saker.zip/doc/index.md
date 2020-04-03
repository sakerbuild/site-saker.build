# Overview

The [saker.zip](https://nest.saker.build/package/saker.zip) package in the [saker.nest repository](root:/saker.nest/index.html) provides features for [ZIP archive](https://en.wikipedia.org/wiki/Zip_(file_format)) creation using the [saker.build system](root:/saker.build/index.html).

The [`saker.zip.create()`](/taskdoc/saker.zip.create.html) task allows you to define the contents of a ZIP archive that will be constructed during a build execution. You can specify the contents using the parameters of the task.

The task supports adding resources from directories, including files from other archives, and allows arbitrary transformations to be executed during the archiving process.

The task creates a deterministic output by default, that is achieved by setting the same modification time for all the entries in the created archive.

The simplest example for getting started is:
 
```sakerscript
saker.zip.create(
	Resources: {
		Directory: res,
		Resources: **
	}
)
```

The above will create a ZIP archive that contains all the files from the `res` directory. For further examples see [](gettingstarted.md) and [Examples](examples/index.md).
