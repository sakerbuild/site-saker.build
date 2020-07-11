# Compiling

The [`saker.msvc.ccompile()`](/taskdoc/saker.msvc.ccompile.html) task allows compiling C/C++ source files into object files. The task can be used to compile for the local machine, or for any other supported architectures.

The task serves as a frontend for [`cl.exe`](https://docs.microsoft.com/en-us/cpp/build/reference/compiling-a-c-cpp-program?view=vs-2019) present in the MSVC toolchain.

The simplest example for compilation is the following:

```sakerscript
saker.msvc.ccompile(src/**/*.cpp)
```

The above compiles all source files in the `src` directory that end with `.cpp`. The compilation language is automatically determined by the extension of the source file (or using the [`Language`](/taskdoc/types/CompilationInputPassTaskOption.html#f-Language) option). You can also specify various options for the compiled files:

```sakerscript
saker.msvc.ccompile(
	{
		Files: src/**/*.cpp,
		MacroDefinitions: {
			MY_MACRO: 123
		}
	}
)
```

In the above, the `MY_MACRO` will be defined for the preprocessor with the value `123`. You can also specify different inputs and different configurations for them:

```sakerscript
saker.msvc.ccompile([
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

In the above, the macro definition for `MY_MACRO` will be set to `456` for the compiled C files.

## Output

The output of the compilation is placed in the build directory. It will be under the `{build-dir}/saker.msvc.ccompile/{compilation-id}/{architecture}` directory where `{compilation-id}` is the [compilation identifier](#compilation-identifier) of the compilation pass and `{architecture}` is the target [architecture](#architecture) of the compilation.

## Compilation identifier

The compilation identifier (and the target architecture) uniquely identifies a given compilation pass during the build system execution. It is used to distinguish the different compilations, their outputs, and to prevent accidental overwriting of each others outputs. The `Identifier` parameter can be used to set a custom one:

```sakerscript
saker.msvc.ccompile(
	Input: #### ... ###,
	Identifier: main
)
```

The compilation identifier is also used to determine if any [compiler options](compileroptions.md) need to be merged for the compilation pass.

## Architecture

The compilation can be done in order to target a given architecture. The [`saker.msvc.ccompile()`](/taskdoc/saker.msvc.ccompile.html) task can be instructed to compile for a given architecture by specifying the `Architecture` parameter:

```sakerscript
saker.msvc.ccompile(
	Input: #### ... ###,
	Architecture: x64
)
```

If no architecture is specified, the compilation will be executed targetting the architecture of the local machine.
