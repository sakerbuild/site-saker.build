# Best practices

The following article includes best practices that we've noticed to be useful when interacting with the build system. This includes build script, configuration, and other hopefully useful recommendations. This article will be extended over time as we encounter more and more use-cases.

## Don't overwrite files

You should **not** configure your build process in a way that may cause files to be overwritten. Overwriting files may cause many issues such as non-reproducible builds, non-deterministic builds and unnecessary incremental rebuilds.

In general, build tasks use their own dedicated output directories to produce their files results. (E.g. the [`saker.java.compile()`](root:/saker.java.compiler/taskdoc/saker.java.compile.html) task writes the results to a compilation private output directory in the build directory.) In most cases you don't need to manually deal with this as the separation is usually ensured by the build tasks themselves.

## Don't share file output locations

You should **not** configure the build tasks in a way that they produce their output files to the same location. If they do, the tasks could silently overwrite each others output files, causing non-reproducible builds, non-deterministic builds, and unnecessary incremental rebuilds.

## Input and output locations shouldn't overlap

You should **not** configure the build tasks in a ways that their outputs are produced to a location that is a possible input of theirs. If you configure a task that it writes the outputs to a location that is also the input of the same task, that may cause incorrect incremental builds.

## Don't hardcode task output locations

You should **not** use direct paths to outputs of build tasks if you expect them to be part of the input of one. An example:

```sakerscript
# !!! THIS IS BAD, don't do this !!!
saker.java.compile(
	SourceDirectories: src,
	Identifier: main,
)
saker.jar.create(
	Resources: {
		Directory: "builddir:/saker.java.compile/main/bin",
		Resources: **/*.class,
	}
)
# !!! THIS IS BAD, don't do this !!!
```

You can see that we compile the Java sources in the `src` directory, and want to package the `class` files into a `jar` Java archive. The above code **will not work** as the `saker.jar.create()` task may run before the compilation task therefore not seeing the produced `class` files.

The correct way of doing the above is:

```sakerscript
$javac = saker.java.compile(
	SourceDirectories: src,
	Identifier: main,
)
saker.jar.create(
	Resources: {
		Directory: $javac[ClassDirectory],
		Resources: **/*.class,
	}
)
```

You can see that we've replaced the hardcoded output location with the `$javac[ClassDirectory]` expression. This will cause the `saker.jar.create()` task to take the directory path from the Java compilation task. As the directory path is only available to be used *after* the Java compilation task completes, this will produce the expected output, as the `jar` creation will always wait the Java compilation to be done.
