# Build targets

Each build file consist of zero or more build targets. This allows developers to describe different actions for different required build artifacts.

In [SakerScript](/doc/scripting/index.md) (the built-in language), you can just start writing your build actions without declaring a build target. It will be implicitly declared with the name "build" if omitted:

```sakerscript
saker.java.compile(src)
```

When the above build file is executed, it will call the [`saker.java.compile()`](root:/saker.java.compiler/taskdoc/saker.java.compile.html) build task. It can also be declared in build targets as follows:

```sakerscript
compile {
	saker.java.compile(src)
}
```

In this case the `compile` target needs to be invoked to compile the sources. Note that multiple build targets can be declared in a file:

```sakerscript
compile {
	saker.java.compile(src)
}
test {
	example.test.project()
}
```

In this case developers can invoke different targets based on what they're set out to do. Build targets can also be invoked from the build file itself, using the [`include()`](/doc/scripting/builtintasks/include.md) task.

```sakerscript
export {
	example.export.project()
}
test {
	include(export)
	example.test.project()
}
```

When `test` is invoked in the above example, it will include the `export` build target, and call `example.test.project()` fictional task as well. Build targets can also have input and output parameters which help the composition of different tasks and results:

```sakerscript
compile(
	out CompilationResult,
) {
	$CompilationResult = saker.java.compile(src)
}
export {
	$compile = include(compile)
	example.export.project(Results: $compile[CompilationResult])
}
```

When `export` is called in the above example, it will include the `compile` target. That will compile the appropriate sources, and return the result of the compilation via assigning the output parameter variable `CompilationResult`. The `export` target then use the result of the `compile` target and export the project using the value of the `CompilationResult` output parameter.

If you know that the [`saker.java.compile()`](root:/saker.java.compiler/taskdoc/saker.java.compile.html) task is always called when any of the build targets are invoked in a file, you can also export the compilation task call to the global expression scope:

```sakerscript
static(CompilationResult) = saker.java.compile(src)
compile {
	# compilation is invoked automatically by the above global expression
}
export {
	export.project(Results: static(CompilationResult))
}
```

The expressions in the global scope will always be invoked if a build target is called in the script file, and this can result in a more concise code. The results of the compilation is conveyed to the `export` target using a file-level [static](/doc/scripting/builtintasks/static.md) variable. See [](/doc/scripting/langref/sourcefile/index.md#global-expressions) for more information.

Build targets also support input parameters which are declared the same way as output parameters, but with the `in` keyword:

```sakerscript
compile(
	in Directory,
	out CompilationResult,
) {
	$CompilationResult = saker.java.compile(Directory: $Directory)
}
all {
	include(compile, Directory: src)
	include(compile, Directory: test)
}
```

The `compile` target in the above example compiles some sources in the directory that is specified by the `Directory` parameter. It is passed to the target in the `include()` calls as an extra named parameter.

Build target parameters can also have default values. See [](/doc/scripting/langref/sourcefile/index.md#parameters) for more information.