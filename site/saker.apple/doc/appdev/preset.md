# Configuration preset

The saker.apple package doesn't provide compilation related features. Instead, it allows you to create compilation configuration for invoking [clang](root:/saker.clang/doc/index.html).

The [`saker.apple.preset()`](/taskdoc/saker.apple.preset.html) lets you create configurations based on a few parameters to invoke clang with the proper compilation flags:

```sakerscript
$preset = saker.apple.preset(
	iphoneos,
	Architecture: armv7,
	PlatformVersionMin: "7.0",
)
$compile = saker.clang.compile(
	src/**/*.mm,
	CompilerOptions: $preset,
)
$link = saker.clang.link(
	$compile,
	LinkerOptions: $preset,
)
```

In the above, we create a preset that contains the configurations for creating iPhone applications running on armv7 architecture and have a minimum OS version of 7.0.

The preset then passed as the options parameter for the clang invocation tasks. The compilation flags that are required for compiling iPhone applications will be added to clang as expected.

See the [`saker.apple.preset()`](/taskdoc/saker.apple.preset.html) task documentation for other configuration options.

## Info.plist value insertion

In order to be able to distribute your applications, you may need to insert values into the *Info.plist* file of your application that is essential for your app execution or describe the build environment. \
The App Store may reject an update if some values are missing.

You can use the result of [`saker.apple.preset()`](/taskdoc/saker.apple.preset.html) to insert these values in your *Info.plist*:

```sakerscript
$preset = saker.apple.preset(### ... ###)
saker.plist.insert(
	Info.plist, 
	Values: $preset[InfoPlistValues],
	SDKs: $preset[SDKs],
)
```

The above will cause appropriate values to be inserted into the specified *Info.plist*. Some notable examples are:

* CFBundleSupportedPlatforms
	* Contains an array of platform names that the application can run on. Automatically determined based on the target platform.
* MinimumOSVersion
	* The minimum OS version based on the `PlatformVersionMin` parameter.
* DT*
	* Various development tools related entries. E.g. DTXcode, DTSDKName, DTPlatformName, ...
* BuildMachineOSBuild
	* The build version of the macOS you're developing on.
