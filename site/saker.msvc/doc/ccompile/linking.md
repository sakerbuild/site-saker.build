# Linking

The [`saker.msvc.clink()`](/taskdoc/saker.msvc.clink.html) task allows linking compiled object files and other supported inputs. The task can be used to link for the local machine, or for any other supported architectures.

The task serves as a frontend for the [`link.exe`](https://docs.microsoft.com/en-us/cpp/build/reference/linking?view=vs-2019) present in the MSVC toolchain.

The simplest example for linking is the following:

```sakerscript
$ccompile = saker.msvc.ccompile(### ... ###)
saker.msvc.clink($ccompile)
```

The above takes the result of a compilation and performs the linking for the output object files. The target architecture, and identifier will be inferred from the compilation task output.

The task allows specifying various options on how the linking should be done:

```sakerscript
saker.msvc.clink(
	Input: ### ... ###,
	SimpleParameters: [ /DLL, /LTCG ]
)
```

The above will cause the task to pass the [`/DLL`](https://docs.microsoft.com/en-us/cpp/build/reference/dll-build-a-dll?view=vs-2019) and [`/LTCG`](https://docs.microsoft.com/en-us/cpp/build/reference/ltcg-link-time-code-generation?view=vs-2019) parameters directly to the backend [`link.exe`](https://docs.microsoft.com/en-us/cpp/build/reference/linking?view=vs-2019).

## Output

The output of the linking is placed in the build directory. It will be under the `{build-dir}/saker.msvc.clink/{compilation-id}/{architecture}` directory where `{compilation-id}` is the [compilation identifier](#compilation-identifier) of the linker pass and `{architecture}` is the target [architecture](#architecture) of the compilation.

## Compilation identifier

The compilation identifier (and the target architecture) uniquely identifies a given linker pass during the build system execution. It is used to distinguish the different linking operations, their outputs, and to prevent accidental overwriting of each others outputs. The `Identifier` parameter can be used to set a custom one:

```sakerscript
saker.msvc.clink(
	Input: #### ... ###,
	Identifier: main
)
```

The compilation identifier is also used to determine if any [linker options](linkeroptions.md) need to be merged for the linker pass.

## Architecture

The linking of the inputs can be done for a given target architecture. The [`saker.msvc.clink()`](/taskdoc/saker.msvc.clink.html) task can be instructed to link for a given architecture by specifying the `Architecture` parameter:

```sakerscript
saker.msvc.clink(
	Input: #### ... ###,
	Architecture: x64
)
```

If no architecture is specified, the linker task will attempt to infer the target architecture based on the inputs. If that cannot be done, the architecture of the local machine is used.
