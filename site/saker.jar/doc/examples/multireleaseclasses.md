# Multi-release Java classes

See also: [](../jarcreation/multirelease.md), [](classes.md)

In the following example we use the [`saker.java.compile()`](root:/saker.java.compiler/taskdoc/saker.java.compile.html) task to compile Java sources and include it in the Java archive. We'll compile the codebase for multiple target JDK versions and include them in the created JAR file. The following example uses the [compiler options](root:/saker.java.compiler/doc/javacompile/compileroptions.html) to set an appropriate JDK for each task.

```sakerscript
$options = "__TOKEN__"# ... the compilation options ...
$multireleasecontents = foreach $jdk in [8, 9, 10] 
		with $javac {
	$javac = saker.java.compile(
		SourceDirectories: src,
		Identifier: "jdk{ $jdk }",
		CompilerOptions: $options
	)
} : {
	$jdk: {
		Resources: {
			Directory: $javac[ClassDirectory],
			Resources: **
		}
	}
}
saker.jar.create(
	MultiReleaseContents: $multireleasecontents
)
```

The JAR archive creation part of the above example simply passes the `MultiReleaseContents` parameter as its input. However, the compilation for different JDKs are more complex:

We use a `foreach` loop to compile the Java sources in the `src` directory for Java major versions 8, 9, and 10. The result expression of the `foreach` constructs a map that has the major versions of the target Java versions as its key, and the ZIP content specification for the value.

The created Java archive will have the following content structure:

```plaintext
<classes for JDK8>
META-INF/versions/9/<classes for JDK9>
META-INF/versions/10/<classes for JDK10>
``` 
