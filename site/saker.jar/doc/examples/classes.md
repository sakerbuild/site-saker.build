# Java classes

In the following example we use the [`saker.java.compile()`](root:/saker.java.compiler/taskdoc/saker.java.compile.html) task to compile Java sources and include it in the Java archive:

```sakerscript
$javac = saker.java.compile(src)
saker.jar.create(
	Resources: {
		Directory: $javac[ClassDirectory],
		Resources: **
	}
)
```

In the above, we compile the Java sources in the `src` directory. We then retrieve the output class directory that contains the compilation result, and pass it as an input to the Java archive.

**Note** that if the Java compilation uses class paths, the class files from the class paths are not included in the JAR.
