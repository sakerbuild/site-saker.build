# Java compile classpath

Passing saker.nest bundles as an input to Java compilation can be done using the following ways. For proper operation, we recommend using the [compile dependency filter](compilefilter.md) when resolving the bundles.

#### `saker.java.classpath.bundle()`

The [`saker.java.classpath.bundle()`](root:/saker.java.compiler/taskdoc/saker.java.classpath.bundle.html) task can be used to create a classpath for the given bundles. It is the recommended approach to create a classpath. It also retrieves the source attachments of the bundles when building inside an IDE.

See [](root:/saker.java.compiler/doc/examples/nestbundleclasspath.html) for usage example.

#### Localized bundles

You can [localize](../bundlemanagement/localize.md) the given bundles, and then pass them to the [`saker.java.compile()`](root:/saker.java.compiler/taskdoc/saker.java.compile.html) as local path references:

```sakerscript
$localized = nest.bundle.localize(### ... ###)
saker.java.compile(
	SourceDirectories: src,
	ClassPath: foreach $path in $localized[BundleLocalPaths]: 
		[ std.file.local($path) ]
)
```

This way the bundles don't have to be copied into the build system file hierarchy.

#### Downloaded bundles

```sakerscript
$downloaded = nest.bundle.download(### ... ###)
saker.java.compile(
	SourceDirectories: src,
	ClassPath: foreach $path in $downloaded[BundlePaths]: 
		[ $path ]
)
```

The above will [download](../bundlemanagement/download.md) the bundles, and the pass their paths as input to the Java compiler task.
