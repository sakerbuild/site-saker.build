# `static()`

The `static()` task can be used to access a static variable. Static variables exist on a file-level scope, and is different from the regular variables known in the language. The static variables can only be accessed using this task. They can be retrieved and assigned.

The result of the `static()` task can be used on the left hand side of an [assignment expression](/doc/scripting/langref/operators/assignment/index.md).

It is generally useful to declare file-level constants using static variables among the global expressions. It can be also used to store the results of tasks which should be only invoked once, when any of the targets are called in a build file.

## Parameters

The following parameters are used by the task:

| Parameter 	       	| Description   	|
|----------------------	|---------------	|
| unnamed	| **Required** name of the static variable that is being accessed.	|

## Task result

The reference to the static variable.

## Example

```sakerscript
# declare file-level constants
static(TEST_PARAMETER) = 123
static(PACKAGE_VERSION) = 1.0
# compile some sources when a build target is invoked in this file
static(CompilationResult) = example.compile.sources()

export {
	# export the compiled binaries in some way
	example.export.compilation.result(
		static(CompilationResult),
		Path: "export-v{ static(PACKAGE_VERSION) }.pkg",
	)
}
test {
	# test the result of the compilation
	example.test.compilation.result(
		static(CompilationResult), 
		TestParam: static(TEST_PARAMETER),
	)
}
```

In the above example we can see that `example.compile.sources()` task will compile the sources whenever any of the build targets of the file is invoked. This global declaration is useful, as you don't have to repeat code in both `export` and `test` build targets, and don't have to manually [`include()`](include.md) some `compile` build target to have the sources compiled.

The constants `TEST_PARAMETER` and `PACKAGE_VERSION` is declared at the top of the file, so you don't have to search the file when you need to modify these values, but can do it in one place.

As a convention, it is recommended to name configuration related constants in `UPPER_SNAKE_CASE` naming format.

<small>

The tasks in the above example is fictional and serve educational purposes.

</small>