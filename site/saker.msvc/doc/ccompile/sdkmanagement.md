# SDK management

The tasks in the saker.msvc package use the [saker.sdk.support](root:/saker.sdk.support/index.html) package for managing the SDKs that they work with. SDKs can be used to specify which MSVC installation to be used when compiling the sources, or to specify inputs based on them.

Both the compiler and linker task work with the SDK named `MSVC`. They will locate the [`cl.exe`](https://docs.microsoft.com/en-us/cpp/build/reference/compiling-a-c-cpp-program?view=vs-2019) and [`link.exe`](https://docs.microsoft.com/en-us/cpp/build/reference/linking?view=vs-2019) which are used to perform their operations based on the specified SDK. If no SDKs are specified with the name `MSVC`, the tasks will attempt to locate a default one based on the environment they're running on.

To specify an SDK to be used with a given version, you can use the [`saker.msvc.sdk()`](/taskdoc/saker.msvc.sdk.html) task:

```sakerscript
saker.msvc.ccompile(
	Input: ### ... ###,
	SDKs: {
		MSVC: saker.msvc.sdk(14.22.27905)
	}
)
```

The above will cause the task to attempt to locate a Microsoft Visual C++ toolchain installation with the version `14.22.27905`. If the task fails, it will thrown an appropriate error.

## MSVC environment parameters

There may be cases when the package can't find automatically the installed MSVC toolchain installations. In these cases you will need to add an appropriate user parameter for the build environment.

You can specify the `saker.msvc.sdk.install.location.<version>` environment parameter with the path to the install location of the toolchain for the given version.

For example:

```plaintext
saker.msvc.sdk.install.location.14.22.27905=c:\Program Files (x86)\Microsoft Visual Studio\2019\Community\VC\Tools\MSVC\14.22.27905
```

### Legacy toolchain layout

The tasks support the legacy layout of the MSVC installations. The installation locations that are used with Visual Studio 15 or older versions are considered to be *legacy*. More information about layout differences: [Compiler Tools Layout in Visual Studio 2017 | C++ Team Blog](https://devblogs.microsoft.com/cppblog/compiler-tools-layout-in-visual-studio-15/).

In order to specify a legacy layout, use the `saker.msvc.legacy.sdk.install.location.<version>` environment parameter:

```plaintext
saker.msvc.legacy.sdk.install.location.14.0=c:\Program Files (x86)\Microsoft Visual Studio 14.0\VC
```

You can reference legacy layout SDKs using the `LegacyVersion` parameter:

```sakerscript
saker.msvc.ccompile(
	Input: ### ... ###,
	SDKs: {
		MSVC: saker.msvc.sdk(LegacyVersion: 14.0)
	}
)
```

In these cases the version is considered to be the one as in the `Microsoft Visual Studio <version>` parent directory.

## Windows kits

When developing with the MSVC toolchain, you are most likely to use the Windows SDK. The Windows SDK contains the necessary headers, libraries and other resources to develop for the Windows operating system. (E.g. `Windows.h`)

An SDK configuration for Windows Kits can be retrieved using the [`saker.windows.sdk.windowskits()`](root:/saker.windows/taskdoc/saker.windows.sdk.windowskits.html) task. It's provided by the [saker.windows](root:/saker.windows/doc/index.html) package.

An example for using it:

```sakerscript
saker.msvc.ccompile(
	Input: {
		Files: src/**/*.cpp,
		IncludeDirectories: [
			sdk.path(WindowsKits, Identifier: include.shared),
			sdk.path(WindowsKits, Identifier: include.ucrt),
			sdk.path(WindowsKits, Identifier: include.um)
		]
	},
	SDKs: {
		WindowsKits: saker.windows.sdk.windowskits(10.0.18362.0)
	}
)
```

The above example uses the Windows Kits SDK with the version of `10.0.18362.0`, and adds some include directories from it for the compilation.

Note that the WindowsKits SDK is not an absolute requirement for the compilation and linker tasks to work, however, you most likely will need it when using them.

The [`saker.msvc.coptions.preset()`](/taskdoc/saker.msvc.coptions.preset.html) when used will add the `WindowsKits` SDK if necessary to the tasks if not specified explicitly by the developer.

See [Windows Kits SDK](root:/saker.windows/doc/sdkmanagement/windowskits.html) for more information.
