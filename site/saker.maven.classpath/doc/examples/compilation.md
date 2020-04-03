# Java compilation

You can pass the result of the [`saker.maven.classpath()`](/taskdoc/saker.maven.classpath.html) task as an input to the [`saker.java.compile()`](root:/saker.java.compiler/taskdoc/saker.java.compile.html) task for its classpath:

```sakerscript
saker.java.compile(
	SourceDirectories: src,
	ClassPath: saker.maven.classpath([
		"junit:junit:4.12",
		"org.slf4j:slf4j-api:1.7.19"
	])
)
```

It can be also used alongside other various classpaths:

```sakerscript
saker.java.compile(
	SourceDirectories: src,
	ClassPath: [
		lib/mylib.jar,
		saker.maven.classpath(### ... ###)
	]
)
```

