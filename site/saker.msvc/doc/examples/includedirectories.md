# Include directories

The `IncludeDirectories` property for the [`saker.msvc.ccompile()`](/taskdoc/saker.msvc.ccompile.html) task can be used to specify include paths where the compiler will search for `#include`d files:

```sakerscript
saker.msvc.ccompile(
	{
		Files: src/**/*.cpp,
		IncludeDirectories: include
	}
)
```

The above simply adds the `include` subdirectory of the current working directory to the current include search path. The specified directories are passed to the compiler using the [`/I`](https://docs.microsoft.com/en-us/cpp/build/reference/i-additional-include-directories?view=vs-2019) parameter.

You can also add SDK paths as include directories. You may need to add the include directory of the MSVC toolchain if you don't use any [option presets](../ccompile/optionpresets.md):

```sakerscript
saker.msvc.ccompile(
	{
		Files: src/**/*.cpp,
		IncludeDirectories: [
			sdk.path(MSVC, Identifier: include)
		]
	}
)
```

The associated SDK with the name `MSVC` is implicitly added by the task implementation.
