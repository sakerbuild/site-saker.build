# JNI compilation

The following example showcases how you can compile a native library alongside your Java sources. We'll be using the [`saker.msvc.ccompile()`](root:/saker.msvc/taskdoc/saker.msvc.ccompile.html) task to compile the C sources, and use [`saker.msvc.clink()`](root:/saker.msvc/taskdoc/saker.msvc.clink.html) to link the object files (from the [saker.msvc](root:/saker.msvc/index.html) package). These tasks use the Microsoft Visual C++ compiler.

The example also uses the [saker.sdk.support](root:/saker.sdk.support/index.html) package.

```sakerscript
$javac = saker.java.compile(
	SourceDirectories: src,
	GenerateNativeHeaders: true
)
$ccompile = saker.msvc.ccompile(
	{
		Files: csrc/**/*.cpp,
		IncludeDirectories: [
			sdk.path(java, Identifier: include),
			sdk.path(java, Identifier: include.platform),
			$javac[HeaderDirectory]
		]
	},
	SDKs: {
		Java: $javac[JavaSDK]
	},
	CompilerOptions: [
		saker.msvc.coptions.preset(dll)
	]
)
saker.msvc.clink(
	$ccompile,
	LinkerOptions: [
		saker.msvc.coptions.preset(dll)
	]
)
```

The above example assumes that the Java sources are in the `src` directory, while the native sources that the library consists of is under the `csrc` directory.

We'll simply compile our Java sources with [native header generation](nativeheaders.md) enabled. Later, we need to pass the include directories and the associated JDK to the C compiler so it can find the appropriate platform headers. We also pass the output header directory from our compilation process using `$javac[HeaderDirectory]`.

The `include.platform` SDK path identifier corresponds to the include directory that is associated with the current operating system that the build is running on. If you want a platform specific directory, you can use any of the following instead of `include.platform`: `include.win32` (Windows), `include.darwin` (macOS), `include.linus` (Linux), or `include.solaris` (Solaris).\
The `include` path identifier corresponds to the JDK `include` directory that contains `jni.h`.

We need to pass the Java SDK to the C compile task that our compilation task used, so it can resolve the above path identifiers for the include directories.

The [`saker.msvc.coptions.preset()`](root:/saker.msvc/taskdoc/saker.msvc.coptions.preset.html) task loads some predefined options that causes the compiler and linker to create a native library. (DLL)

You may want to further adapt the example for release, debug builds, and based on your needs.