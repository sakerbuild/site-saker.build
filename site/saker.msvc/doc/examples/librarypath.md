# Library path

The `LibraryPath` property for the [`saker.msvc.clink()`](/taskdoc/saker.msvc.clink.html) task can be used to specify library paths where the linker will search for library files:

```sakerscript
saker.msvc.clink(
	Input: ### ... ###,
	LibraryPath: lib
)
```

The above simply adds the `lib` subdirectory of the current working directory to the current library search path. The specified directories are passed to the linker using the [`/LIBPATH`](https://docs.microsoft.com/en-us/cpp/build/reference/libpath-additional-libpath?view=vs-2019) parameter.

You can also add SDK paths as library paths. You may need to add the library path of the MSVC toolchain if you don't use any [option presets](../ccompile/optionpresets.md):

```sakerscript
saker.msvc.clink(
	Input: ### ... ###,
	LibraryPath: [
		sdk.path(MSVC, Identifier: lib.x64)
	]
)
```

The above assumes that you're linking for the `x64` architecture.

The associated SDK with the name `MSVC` is implicitly added by the task implementation.
