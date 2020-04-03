# Localized artifacts

The [`saker.maven.classpath()`](/taskdoc/saker.maven.classpath.html) task can take the result of the [`saker.maven.localize()`](root:/saker.maven.support/taskdoc/saker.maven.localize.html) task as its input:

```sakerscript
saker.java.compile(
	SourceDirectories: src,
	ClassPath: saker.maven.classpath(saker.maven.localize([
		"junit:junit:4.12",
		"org.slf4j:slf4j-api:1.7.19"
	]))
)
```

This works the same way as [](artifacts.md), but you don't have to specify the artifact coordinates multiple times.

If you localize the artifacts separately, the use-case can be simplified:

```sakerscript
$artifactloc = saker.maven.localize([
	"junit:junit:4.12",
	"org.slf4j:slf4j-api:1.7.19"
])
saker.java.compile(
	SourceDirectories: src,
	ClassPath: saker.maven.classpath($artifactloc)
)
```

