# Artifact localization

Artifact localization is the process of making the Maven artifacts available on the *local* machine. The [`saker.maven.localize()`](/taskdoc/saker.maven.localize.html) tasks provide this feature and makes the specified artifacts available in the local repository.

This task differs from [`saker.maven.download()`](/taskdoc/saker.maven.download.html) in that it doesn't copy the artifacts into the file system of the build execution.

The task taskes a list of artifacts as its input:

```sakerscript
saker.maven.localize(
	Artifacts: [
		"junit:junit:4.12",
		"org.slf4j:slf4j-api:1.7.19"
	]
)
```

The above task will make the specified artifacts available in the local repository. It may require for them to be downloaded from remote repositories.

Note that the task doesn't perform any dependency resolution. In order to do that, you can use the [`saker.maven.resolve()`](/taskdoc/saker.maven.resolve.html) task as follows:

```sakerscript
saker.maven.localize(
	Artifacts: saker.maven.resolve([
		"junit:junit:4.12",
		"org.slf4j:slf4j-api:1.7.19"
	])
)
```

While the first example doesn't include the dependencies, the latter will have the `org.hamcrest:hamcrest-core:jar:1.3` dependent artifact localized as well.
