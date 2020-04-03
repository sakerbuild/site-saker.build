# Java classpath

First of all, we recommend using the [saker.maven.classpath](root:/saker.maven.classpath/index.html) package for dealing with Maven artifacts as Java classpath. However, if you can't use that package, the following examples may apply:

#### Localized classpath

If the receiver task supports it, you can specify the class path using localization:

```sakerscript
$localized = saker.maven.localize([
	"org.slf4j:slf4j-api:1.7.19"
])
saker.java.compile(
	SourceDirectories: src,
	ClassPath: foreach $path in $localized[ArtifactLocalPaths]: 
		[ std.file.local($path) ] 
)	
``` 

In the above, we localize the artifact with the coordinates of `org.slf4j:slf4j-api:1.7.19`. Then we use the localization result to convert the local paths of the artifacts to a local file reference. The [`std.file.local()`](root:/saker.standard/taskdoc/std.file.local.html) task is used to create a file path reference on the local file system.

Note that the above requires that the receiver task supports classpath input using local files.

This solution doesn't support automatic downloading of attached source artifacts.

#### Downloaded classpath

If the receiver task doesn't support local paths as the classpath input, you may need to download the artifacts for the build execution:

```sakerscript
$downloaded = saker.maven.download([
	"org.slf4j:slf4j-api:1.7.19"
])
saker.java.compile(
	SourceDirectories: src,
	ClassPath: foreach $path in $downloaded[ArtifactPaths]: 
		[ $path ] 
)
```

The above is similar to the [](#localized-classpath) example, but doesn't need the paths to be converted to local file references, as the artifacts are available in the build file system.

This solution has the disadvantage of requiring the artifacts to be copied for the build execution.

This solution doesn't support automatic downloading of attached source artifacts.

#### `saker.maven.classpath()`

The [`saker.maven.classpath()`](root:/saker.maven.classpath/taskdoc/saker.maven.classpath.html) task can be used to create a classpath object for the artifacts. The task also supports automatic downloading of associated source artifacts. If you're using the build system in an IDE, it can be used to automatically set the sources for the classpath JAR files making code browsing easier.

Please see the examples in the [saker.maven.classpath](root:/saker.maven.classpath/index.html) package.
