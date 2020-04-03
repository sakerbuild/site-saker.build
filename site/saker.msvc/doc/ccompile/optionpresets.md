# Option presets

Option presets are predefined configurations that can be applied to compiler and linker tasks. They are provided by the [`saker.msvc.coptions.preset()`](/taskdoc/saker.msvc.coptions.preset.html) task and they can be applied to the `CompilerOptions` parameter for C compilation and `LinkerOptions` parameter for linking.

The purpose of presets is to ease common configurations instead of requiring the developer to manually configure the [`cl.exe`](https://docs.microsoft.com/en-us/cpp/build/reference/compiling-a-c-cpp-program?view=vs-2019) and [`link.exe`](https://docs.microsoft.com/en-us/cpp/build/reference/linking?view=vs-2019) arguments.

An example that uses the preset for creating a dynamic link library:

```sakerscript
$ccompile = saker.msvc.ccompile(
	src/**/*.cpp,
	CompilerOptions: [
		saker.msvc.coptions.preset(dll),
	]
)
$procclink = saker.msvc.clink(
	$ccompile,
	LinkerOptions: [
		saker.msvc.coptions.preset(dll),
	]
)
```

The preset with the name `dll` can be used to get a preset configuration that when applied to the compilation and linker tasks will produce a DLL output.

The use of presets are optional, and the tasks may be configured the same way to the full extent without them.
