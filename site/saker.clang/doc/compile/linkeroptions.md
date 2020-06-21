# Linker options

The [`saker.clang.link()`](/taskdoc/saker.clang.link.html) task allows dynamic option merging based on the identifier of the linker operation. It can be used to define the linker options in one place that can be applied to one or more linker operations selectively.

The following example uses different library paths for linking based on the type of app we're about to create:

```sakerscript
$options = [
	{
		Identifier: demo,
		LibraryPath: libs/demo,
	},
	{
		Identifier: main,
		LibraryPath: libs/main,
	},
]
saker.clang.link(
	Input: ### ... ###,
	Identifier: app-demo,
	LinkerOptions: $options
)
saker.clang.link(
	Input: ### ... ###,
	Identifier: app-main,
	LinkerOptions: $options
)
```

In the above, the library path `libs/demo` will be used to link the binaries for the `app-demo` linker pass, and will use `libs/main` library path to link the binaries for `app-main`.

A linker option entry will be merged with the linker operation if all of the dash separated identifier parts are contained in the operation identifier of the task.
