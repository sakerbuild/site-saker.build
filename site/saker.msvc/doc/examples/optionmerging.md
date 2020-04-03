# Option merging

See also: [](../ccompile/compileroptions.md), [](../ccompile/linkeroptions.md)

The [`saker.msvc.ccompile()`](/taskdoc/saker.msvc.ccompile.html) and [`saker.msvc.clink()`](/taskdoc/saker.msvc.clink.html) tasks allow you to specify options to be merged with the specified task parameters/properties. These parameters are dynamically merged with the task configuration based on the `Identifier`, `Architecture`, and `Language` of the subject.

The feature allows you to configure the tasks in a way that is independent from the location of the task invocation:

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

## Merging criteria

The mergeability of the options are determined based on the associated `Identifier`, `Language`, and `Architecture` of the options. The options can be defined in the following way:

```sakerscript
{
	Architecture: x64,
	Language: C++,
	Identifier: app,
	# ... other options
}
```

The above option will be only merged if the compilation is being done using `C++` language, and if the target architecture is `x64`. It also checks if the pass identifier contains the `app` component. The option is able to merged with the following example:

```sakerscript
saker.msvc.ccompile(
	src/**/*.cpp,
	Architecture: x64,
	Identifier: main-app
)
```

If the `Architecture` parameter is not specified, and the inferred target architecture is `x64`, then the option merging will be done in that caes as well.

The option won't be merged with any of the following examples:

```sakerscript
# Different language, C instead of C++
saker.msvc.ccompile(
	src/**/*.c,
	Architecture: x64,
	Identifier: main-app
)
# Different target architecture
saker.msvc.ccompile(
	src/**/*.cpp,
	Architecture: x86,
	Identifier: main-app
)
# The app identifier component is missing
saker.msvc.ccompile(
	src/**/*.cpp,
	Architecture: x64,
	Identifier: main
)
```

Note that the above option can be also merged to the [`saker.msvc.clink()`](/taskdoc/saker.msvc.clink.html) task. In those cases, the `Language` criteria is not taken into account.

If an option doesn't have some criteria defined for it, then that won't be taken into account. For example to define an option that can be merged to any task:

```sakerscript
$option = {
	MacroDefinitions: {
		EXAMPLE_APP: 1
	}
}
saker.msvc.ccompile(
	src/**/*.cpp,
	CompilerOptions: $option
)
```

The above will have the `EXAMPLE_APP` macro defined for the compiled files, as the specified option with the macro definitions doesn't have any criteria that limits its merging.

## Compilation input options

The [`saker.msvc.ccompile()`](/taskdoc/saker.msvc.ccompile.html) tasks allows complex configuration of each compiled input set. You can specify different merge options for each input set:

```sakerscript
$options = [ ### ... ### ]
$coreoptions = [ ### ... ### ]
$sideoptions = [ ### ... ### ]
saker.msvc.ccompile(
	Input: [
		{
			Files: src/core/**/*.cpp,
			CompilerOptions: $coreoptions
		},
		{
			Files: src/side/**/*.cpp,
			CompilerOptions: $sideoptions
		}
	],
	CompilerOptions: $options,
	Identifier: main
)
```

The above showcases 3 different option definitions assigned to the appropriate variables. The `$options` will be attempted to be merged to all inputs for the compilation task.

The options specified in `$coreoptions` will be only merged for the compiled files defined by `src/core/**/*.cpp`. \
The options specified in `$sideoptions` will be only merged for the compiled files defined by `src/side/**/*.cpp`.

Specifying compiler options in the `CompilerOptions` property for an input set can be used to apply options privately only to that given input set.

You can also achieve this by using the `SubIdentifier` property:

```sakerscript
$options = [
	{
		Identifier: core,
		# ...
	},
	{
		Identifier: side,
		# ...
	},
]
saker.msvc.ccompile(
	Input: [
		{
			Files: src/core/**/*.cpp,
			SubIdentifier: core
		},
		{
			Files: src/side/**/*.cpp,
			SubIdentifier: side
		}
	],
	CompilerOptions: $options,
	Identifier: main
)
```

If any `SubIdentifier` is specified for an input set, then when merging the specified options, the `SubIdentifier` is effectively combined with the `Identifier` parameter if any. Therefore, in the above the merging will commence for the `main-core` and the `main-side` identifiers respectively. The merging will result in the appropriate options being used as specified in `$options`.
