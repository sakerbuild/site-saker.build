# Option presets

See also: [](../ccompile/optionpresets.md)

The option presets allow you to use predefined configuration for the MSVC toolchain in order to develop specific applications. The `saker.msvc.coptions.preset()` task returns an options object that when passed to the compiler and linker tasks as an option, it will configure it appropriately for the given use-case:

```sakerscript
$ccompile = saker.msvc.ccompile(
	src/**/*.cpp,
	CompilerOptions: [
		saker.msvc.coptions.preset(console),
	]
)
$procclink = saker.msvc.clink(
	$ccompile,
	LinkerOptions: [
		saker.msvc.coptions.preset(console),
	]
)
```

In the above example we retrieve the `console` preset and apply it to the compiler and linker tasks. This will result in the appropriate include directories, library paths, and other options to be set for the tasks.

Using presets can be useful if you want to avoid lower level configuration of the backing [`cl.exe`](https://docs.microsoft.com/en-us/cpp/build/reference/compiling-a-c-cpp-program?view=vs-2019) and [`link.exe`](https://docs.microsoft.com/en-us/cpp/build/reference/linking?view=vs-2019).

The available list of presets will grow as the saker.msvc package is being developed.

## `console`

The `console` preset contains configurations that are applicable for creating a simple console application.

It adds the appropriate library paths from the `WindowsKits` SDK. It adds the appropriate include directories from the `MSVC` and `WindowsKits` SDKs.

It also sets the [`/SUBSYSTEM:CONSOLE`](https://docs.microsoft.com/en-us/cpp/build/reference/subsystem-specify-subsystem?view=vs-2019) [simple parameter](simpleparameters.md) for the linker task.

The preset adds the `_CONSOLE` preprocessor definition.

## `dll`

The `dll` present contains configurations for creating a Dynamic Link Library.

It adds the appropriate library paths from the `WindowsKits` SDK. It adds the appropriate include directories from the `MSVC` and `WindowsKits` SDKs.

It also sets the [`/SUBSYSTEM:WINDOWS`](https://docs.microsoft.com/en-us/cpp/build/reference/subsystem-specify-subsystem?view=vs-2019) and [`/DLL`](https://docs.microsoft.com/en-us/cpp/build/reference/dll-build-a-dll?view=vs-2019) [simple parameters](simpleparameters.md) for the linker task.

The preset adds the `_WINDOWS` and '_WINDLL' preprocessor definition.

## `optimize-debug`

The `optimize-debug` preset adds configurations for creating a debug optimized binary.

The preset adds the [`/Od`](https://docs.microsoft.com/en-us/cpp/build/reference/od-disable-debug?view=vs-2019) simple compiler parameter.

The preset adds the `_DEBUG` preprocessor definition.

## `optimize-release`

The `optimize-debug` preset adds configurations for creating a release optimized binary.

The preset adds the [`/GL`](https://docs.microsoft.com/en-us/cpp/build/reference/gl-whole-program-optimization?view=vs-2019), [`/Gy`](https://docs.microsoft.com/en-us/cpp/build/reference/gy-enable-function-level-linking?view=vs-2019), [`/O2`](https://docs.microsoft.com/en-us/cpp/build/reference/o1-o2-minimize-size-maximize-speed?view=vs-2019), [`/Oi`](https://docs.microsoft.com/en-us/cpp/build/reference/oi-generate-intrinsic-functions?view=vs-2019) simple compiler parameters. It also adds the `NDEBUG` preprocessor definition.

The preset adds the [`/LTCG`](https://docs.microsoft.com/en-us/cpp/build/reference/ltcg-link-time-code-generation?view=vs-2019), [`/OPT:REF`](https://docs.microsoft.com/en-us/cpp/build/reference/opt-optimizations?view=vs-2019), [`/OPT:ICF`](https://docs.microsoft.com/en-us/cpp/build/reference/opt-optimizations?view=vs-2019) simple linker parameters. 

## Preset identifier

The presets can be created with a given `Identifier` specified for them:

```sakerscript
$options = [
	saker.msvc.coptions.preset(console, Identifier: con),
	saker.msvc.coptions.preset(dll, Identifier: lib),
]
# Compiling the console app
$conccompile = saker.msvc.ccompile(
	consrc/**/*.cpp,
	CompilerOptions: $options,
	Identifier: main-con
)
saker.msvc.clink(
	$conccompile,
	LinkerOptions: $options,
	Identifier: main-con
)
# Compiling the library
$libccompile = saker.msvc.ccompile(
	libsrc/**/*.cpp,
	CompilerOptions: $options,
	Identifier: main-lib
)
saker.msvc.clink(
	$libccompile,
	LinkerOptions: $options,
	Identifier: main-lib
)
```

If an `Identifier` is specified for the retrieved preset configurations, then they will be only merged with the associated tasks if the `Identifier` of the task contains all the componets as specified in the preset `Identifier`.

In the above example we compile a console application and a library. We store the options that we apply in the `$options` variable. We specified different `Identifier`s for the preset configurations, and this result in that the `con` preset will be only applied to the `main-con` compilation, and the `lib` preset will only apply to the `main-lib` compilation.

Storing options this way can help maintainability of your build script as the configurations are not spread out in your build script, but can be specified in one place.

See also: [](optionmerging.md)