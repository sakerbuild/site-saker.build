# Artifact downloading

Artifact downloading is the process of making the Maven artifacts available for the build execution in the build file system. The task will [localize](localization.md) the artifacts and copy them in the build file system for use. This usually entails that the artifacts will be copied in the build directory.

The task taskes a list of artifacts as its input:

```sakerscript
saker.maven.download(
	Artifacts: [
		"junit:junit:4.12",
		"org.slf4j:slf4j-api:1.7.19"
	]
)
```

Note that the task doesn't perform any dependency resolution. In order to do that, you can use the [`saker.maven.resolve()`](/taskdoc/saker.maven.resolve.html) task as follows:

```sakerscript
saker.maven.download(
	Artifacts: saker.maven.resolve([
		"junit:junit:4.12",
		"org.slf4j:slf4j-api:1.7.19"
	])
)
```

While the first example doesn't include the dependencies, the latter will have the `org.hamcrest:hamcrest-core:jar:1.3` dependent artifact downloaded as well.
