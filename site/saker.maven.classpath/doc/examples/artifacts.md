# Artifact classpath

Creating a classpath for a simple set of artifacts can be done by passing the artifact coordinates to the task:

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
