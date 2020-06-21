# Compiler options

The [`saker.clang.compile()`](/taskdoc/saker.clang.compile.html) task allows dynamic option merging based on the identifier, and compilation language of the inputs. It can be used to define the compilation parameters in one place that can be applied to one or more compilation tasks selectively.

A simple example for this is to define some macros for the preprocessor based on the compilation language:

```sakerscript
$options = [
	{
		Language: C,
		MacroDefinitions: {
			MLANGUAGEID: 1
		}
	},
	{
		Language: C++,
		MacroDefinitions: {
			MLANGUAGEID: 2
		}
	},
]
saker.clang.compile(
	[
		src/**/*.cpp,
		src/**/*.c,
	]
	CompilerOptions: $options
)
```

In the above, the `MLANGUAGEID` macro will be defined when compiling the source files. For every `.c` file, the value of this macro will be 1, while for `.cpp` files it will be 2.

The mergeability of the options is decided based on the compilation `Identifier` and the `Language` of the compiled sources.

This facility can be used to compile various inputs with different options dynamically. For example if we'd like to compile the sources for multiple languages, we can use multiple include directories based on the language:

```sakerscript
$options = [
	{
		Identifier: en,
		IncludeDirectories: inc/en
	},
	{
		Identifier: de,
		IncludeDirectories: inc/de
	},
	{
		Identifier: es,
		IncludeDirectories: inc/es
	}
]
foreach $lang in [en, de, es] {
	saker.clang.compile(
		src/**/*.cpp,
		Identifier: "main-{ $lang }",
		CompilerOptions: $options
	)
}
```


In the above example we compile the source files defined by the `src/**/*.cpp` wildcard, and use different include directories for each compilation pass. If we're compiling for english, the `inc/en` include directory will be used, as the option entry with the `en` identifier is merged with the compilation pass options. (And similarly with other languages.)

A compiler option entry will be merged with the compilation pass if all of the dash separated identifier parts are contained in the compilation identifier of the task.
