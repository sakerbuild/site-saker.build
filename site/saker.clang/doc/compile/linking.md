# Linking

Linking is the step when you take object files that were produced by previously compiled source files and combine them together in a final executable or shared library. The [`saker.clang.link()`](/taskdoc/saker.clang.link.html) task performs this operation:

```sakerscript
$compile = saker.clang.compile(src/**/*.cpp)
saker.clang.link($compile)
```

The above will simply create an executable from the source files in the `src` directory. Passing arguments for clang is possible using the [`SimpleParameters`](/taskdoc/saker.clang.link.html#SimpleParameters) parameter:

```sakerscript
$compile = saker.clang.compile(src/**/*.cpp)
saker.clang.link(
	$compile,
	SimpleParameters: [
		# create a shared library
		-shared
	]
)
```

The above will perform the linking by adding the `-shared` argument for the clang invocation.

## Output

The output of the linking is placed in the build directory. It will be under the `{build-dir}/saker.clang.link/{compilation-id}` directory where `{compilation-id}` is the [compilation identifier](#compilation-identifier) of the linker pass of the compilation.

## Compilation identifier

The compilation identifier uniquely identifies a given linker pass during the build system execution. It is used to distinguish the different linking operations, their outputs, and to prevent accidental overwriting of each others outputs. The `Identifier` parameter can be used to set a custom one:

```sakerscript
saker.clang.link(
	Input: #### ... ###,
	Identifier: main
)
```

The compilation identifier is also used to determine if any [linker options](linkeroptions.md) need to be merged for the linker pass.
