# Local file input

In some cases, you may want to use files from the local file system as the input to other tasks. In order to achieve that you need to reference them as the input to the associated build task:

```sakerscript
saker.java.compile(
	SourceDirectories: src,
	ClassPath: std.file.local("/home/User/lib/mylib.jar")
)
```

The above will cause the [`saker.java.compile()`](root:/saker.java.compiler/taskdoc/saker.java.compile.html) task to use the `/home/User/lib/mylib.jar` JAR file as the input classpath for the compilation.

The [`std.file.local()`](/taskdoc/std.file.local.html) task can be used to reference files on the local file system.