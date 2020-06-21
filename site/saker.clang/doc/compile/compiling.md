# Compiling

The [`saker.clang.compile()`](/taskdoc/index.html) task is used to convert source code into object files. It will invoke clang for each of the input source files with the specified compiler flags.

The simplest example of running it is the following:

```sakerscript
saker.clang.compile(src/**/*.cpp)
```

The above will compile the `.cpp` files in the `src` directory using the C++ language. The language is automatically determined based on the extension of the source file (or using the [`Language`](/taskdoc/types/CompilationInputPassTaskOption.html#f-Language) option).

Other options can be specified for the compilation, such as adding macro definitions:

```sakerscript
saker.clang.compile({
	Files: src/**/*.cpp,
	MacroDefinitions: {
		MY_MACRO: 123
	}
})
```

In the above, the MY_MACRO will be defined for the preprocessor with the value 123. You can also specify different inputs and different configurations for them:

```sakerscript
saker.clang.compile([
	{
		Files: src/**/*.cpp,
		MacroDefinitions: {
			MY_MACRO: 123
		}
	},
	{
		Files: src/**/*.c,
		MacroDefinitions: {
			MY_MACRO: 456
		}
	}
])
```

In the above, the macro definition for MY_MACRO will be set to 456 for the compiled C files.

Parameters can be passed directly to clang by using the [`SimpleParameters`](/taskdoc/types/CompilationInputPassTaskOption.html#f-SimpleParameters) option:

```sakerscript
saker.clang.compile({
	Files: src/**/*.cpp,
	SimpleParameters: [
		# no optimization 
		-O0, 
		# generate debugging information
		-g
	]
})
```

These parameters are directly passed to clang on the command line. If you intend to set arguments that represent files or directories, we recommend using the other options to perform that. (E.g. don't use -I for setting include directories, but the [`IncludeDirectories`](/taskdoc/types/CompilationInputPassTaskOption.html#f-IncludeDirectories) option.)

## Output

The output of the compilation is placed in the build directory. It will be under the `{build-dir}/saker.clang.compile/{compilation-id}` directory where `{compilation-id}` is the [compilation identifier](#compilation-identifier) of the compilation pass.

## Compilation identifier

The compilation identifier uniquely identifies a given compilation pass during the build system execution. It is used to distinguish the different compilations, their outputs, and to prevent accidental overwriting of each others outputs. The `Identifier` parameter can be used to set a custom one:

```sakerscript
saker.clang.compile(
	Input: #### ... ###,
	Identifier: main
)
```

The compilation identifier is also used to determine if any [compiler options](compileroptions.md) need to be merged for the compilation pass.
