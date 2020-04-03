# Usage

The [`saker.maven.classpath()`](/taskdoc/saker.maven.classpath.html) task works on Maven artifacts and creates a classpath object that can be passed to tasks in the [saker.java.compiler](root:/saker.java.compiler/index.html) package and to others which handle classpath input similarly.

The task takes artifact coordinates or already resolved dependencies from the [`saker.maven.resolve()`](root:/saker.maven.support/taskdoc/saker.maven.resolve.html) task. The [`saker.maven.classpath()`](/taskdoc/saker.maven.classpath.html) task **does not** perform dependency resolution.

When used in an IDE, the task automatically downloads the associated source artifacts if available.

## Artifact classpath

Classpath can be created and used for a given set of artifacts:

```sakerscript
saker.java.compile(
	SourceDirectories: src,
	ClassPath: saker.maven.classpath([
		"junit:junit:4.12",
		"org.slf4j:slf4j-api:1.7.19"
	])
)
```

In the above we create a classpath that references the `junit:junit:4.12` and `org.slf4j:slf4j-api:1.7.19` artifacts. Note that the dependencies of the artifacts are **not** resolved.

If the build is invoked in an IDE, the source artifacts are also downloaded.

## Dependency classpath

The task can accept resolved dependencies as its input. Extending the example from above:

```sakerscript
saker.java.compile(
	SourceDirectories: src,
	ClassPath: saker.maven.classpath(saker.maven.resolve([
		"junit:junit:4.12",
		"org.slf4j:slf4j-api:1.7.19"
	]))
)
```

We used the [`saker.maven.resolve()`](root:/saker.maven.support/taskdoc/saker.maven.resolve.html) task to resolve the dependencies of the specified artifacts before passing them to the classpath task. This results in the `org.hamcrest:hamcrest-core:1.3` artifact being included in the classpath as well (`junit` depends on it). Usually this is the way you want to use the task. 

## Repository configuration

The [`saker.maven.classpath()`](/taskdoc/saker.maven.classpath.html) allows specifying how the repositories should be accessed. It works the same way as the [saker.maven.support](root:/saker.maven.support/index.html) package allows configuring the repositories.

More information [here](root:/saker.maven.support/doc/mavensupport/configuration.html). 