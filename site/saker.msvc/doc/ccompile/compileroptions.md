# Compiler options

The [`saker.msvc.ccompile()`](/taskdoc/saker.msvc.ccompile.html) task allows dynamic option merging based on the identifier, architecture, and compilation language of the inputs. It can be used to define the compilation parameters in one place that can be applied to one or more compilation tasks selectively.

A simple example for this is to define some macros for the preprocessor based on the compilation architecture:

```sakerscript
$options = [
	{
		Architecture: x86,
		MacroDefinitions: {
			MARCHITECTURE: X86
		}
	},
	{
		Architecture: x64,
		MacroDefinitions: {
			MARCHITECTURE: X64
		}
	},
]
saker.msvc.ccompile(
	src/**/*.cpp,
	Architecture: x86,
	CompilerOptions: $options
)
saker.msvc.ccompile(
	src/**/*.cpp,
	Architecture: x64,
	CompilerOptions: $options
)
```

The compilation tasks will be set up in a way that the `MARCHITECTURE` preprocessor macro will be defined with the value of `X86` when compiling for `x86` and with the value of `X64` when compiling for `x64` architecture.

The mergeability of the options can also be decided based on the compilation `Identifier` and the `Language` of the compiled sources.

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
	saker.msvc.ccompile(
		src/**/*.cpp,
		Identifier: "main-{ $lang }",
		CompilerOptions: $options
	)
}
```

In the above example we compile the source files defined by the `src/**/*.cpp` wildcard, and use different include directories for each compilation pass. If we're compiling for english, the `inc/en` include directory will be used, as the option entry with the `en` identifier is merged with the compilation pass options. (And similarly with other languages.)

A compiler option entry will be merged with the compilation pass if all of the dot separated identifier parts are contained in the compilation identifier of the task.
