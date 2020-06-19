# Packaging

The contents of your application needs to be packaged into an [*.appx*](https://docs.microsoft.com/en-us/windows/msix/package/packaging-uwp-apps#types-of-app-packages) archive for distribution. Later these *.appx* packages can be put into an [*.appxbundle*](https://docs.microsoft.com/en-us/windows/msix/package/packaging-uwp-apps#types-of-app-packages) that can contain multiple variants of an application (e.g. multiple app packages for different architectures).

The saker.windows package supports both of the above packaging steps. It uses the [makeappx.exe](https://docs.microsoft.com/en-us/windows/win32/appxpkg/make-appx-package--makeappx-exe-) tool under the hood.

## App package

App packages are single archives that contains the application and its resources, targeted at a single device architecture. They can be created using the [`saker.appx.package()`](/taskdoc/saker.appx.package.html) build task. It takes the previously [prepared](preparing.md) contents of your app.

```sakerscript
$prepared = saker.appx.prepare(### ... ###)
saker.appx.package($prepared)
```

The created `.appx` archive will be put in the build directory.

## App bundle

An app bundle is a type of package that can contain multiple app packages, each of which is built to support a specific device architecture. It usually has the *.appxbundle* extension and can be created using the [`saker.appx.bundle()`](/taskdoc/saker.appx.bundle.html) task. It takes the embedded applications as input mapped to their archive paths:

```sakerscript
$pkg_x86 = saker.appx.package(### ... ###)
$pkg_x64 = saker.appx.package(### ... ###)
$pkg_ARM = saker.appx.package(### ... ###)
saker.appx.bundle({
	$pkg_x86[Path]: "MyApp_x86.appx",
	$pkg_x64[Path]: "MyApp_x64.appx",
	$pkg_ARM[Path]: "MyApp_ARM.appx"
})
```

The above creates an *.appxbundle* that contains the app for each architecture. The values for the mappings specify the path a given application will have in the archive. You can also specify the empty path to have it take the name of the input file:

```sakerscript
saker.appx.bundle({
	$pkg_x86[Path]: "",
	$pkg_x64[Path]: "",
	$pkg_ARM[Path]: ""
})
```

You generally only need to set the archive paths in (rare) cases when conflicts occurr.

The created `.appxbundle` will be put in the build directory. 

**Note:** If you're creating the packages for distribution, make sure to [sign](sign.md) the *.appx* before putting them in the *.appxbundle*. 