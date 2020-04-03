# Downloaded artifacts

The [`saker.maven.classpath()`](/taskdoc/saker.maven.classpath.html) task can take the result of the [`saker.maven.download()`](root:/saker.maven.support/taskdoc/saker.maven.download.html) task as its input:

```sakerscript
saker.java.compile(
	SourceDirectories: src,
	ClassPath: saker.maven.classpath(saker.maven.download([
		"junit:junit:4.12",
		"org.slf4j:slf4j-api:1.7.19"
	]))
)
```

This works the same way as [](artifacts.md), but you don't have to specify the artifact coordinates multiple times. The classpath will reference the downloaded artifacts, instead of the ones that are present in the local repository.

If you download the artifacts separately, the use-case can be simplified:

```sakerscript
$artifactdl = saker.maven.download([
	"junit:junit:4.12",
	"org.slf4j:slf4j-api:1.7.19"
])
saker.java.compile(
	SourceDirectories: src,
	ClassPath: saker.maven.classpath($artifactdl)
)
```
