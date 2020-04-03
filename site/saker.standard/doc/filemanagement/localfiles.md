# Local files

Occasionally you may need to work with files that are not accessible from the build execution file hierarchy. This is generally not recommended as the necessary resources should be part of the project you're building, however, in some cases it can be useful.

The [`std.file.local()`](/taskdoc/std.file.local.html) task can be used to create a file reference to a local file. It can be passed as an input to tasks that support it:

```sakerscript
std.file.local("c:/Users/user/Documents/file.txt")
```

The above will create a file reference to the file at `c:/Users/user/Documents/file.txt`. The task doesn't check if the file exists, or has any specific nature. The file reference is just an object that signals that the enclosed path should be interpreted against the local file system.

Note that not all tasks support passing local files as input.

One example for its use-case is passing a local JAR classpath for Java compilation:

```sakerscript
saker.java.compile(
	SourceDirectories: src,
	ClassPath: std.file.local("c:/myrepository/mylibrary.jar")
)
```

The above will cause the Java compilation task to use the specified JAR as an input classpath.

Note that when using build clusters, it is not recommended to use local paths, as that may cause the tasks not to be distributed to the clusters.
