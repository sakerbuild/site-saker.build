# Application bundles

Applications for Apple platforms are contained in bundles. These bundles are single directories (usually have the *.app* extension) with specific contents for the target platform.

The saker.apple package contains different build tasks for setting up the bundle directories for the target platforms.

The actual structure of the application bundles are described in the [Bundle Structure](https://developer.apple.com/library/archive/documentation/CoreFoundation/Conceptual/CFBundles/BundleTypes/BundleTypes.html) section of the Apple documentation.

## Creating an iPhone app bundle

The [`saker.iphoneos.bundle.create()`](/taskdoc/saker.iphoneos.bundle.create.html) task lets you set up the bundle directory with the specified contents:

```sakerscript
$link = saker.clang.link(### ... ###)
saker.iphoneos.bundle.create(
	Contents: [
		Info.plist,
		$link[OutputPath],
		{
			Directory: res,
			Wildcard: **
		}
	]
)
```

The above will create an application bundle with the create executable that was produced by clang, as well as add the resources to the application that are found in the `res` directory. The `Info.plist` file for you application is also included.

## Creating a macOS app bundle

A macOS application bundle has a slightly more complicated structure than the iPhone bundle. Different parts of the application are present in different subdirectories of the bundle.

```sakerscript
$link = saker.clang.link(### ... ###)
saker.macos.bundle.create(
	MacOS: $link[OutputPath],
	Resources: [
		{
			Directory: res,
			Wildcard: **
		}
	],
	Contents: Info.plist,
)
```

The executables are put into the `MacOS` subdirectory, the resources that you application can access are in the `Resources` directory. The *Info.plist* manifest file goes in the `Contents` directory.

The parameters of the [`saker.macos.bundle.create()`](/taskdoc/saker.macos.bundle.create.html) task specify into which subdirectory the specified contents will be put.

## PkgInfo

The [*PkgInfo* file](https://developer.apple.com/library/archive/documentation/MacOSX/Conceptual/BPRuntimeConfig/Articles/ConfigApplications.html) is a special file in the application bundles. It contains 8 characters that describe the type and creator codes of your application or bundle.

The contents of this file should be the concatenation of the `CFBundlePackageType` and `CFBundleSignature` keys that are present in the *Info.plist* file.

The bundle creation tasks will automatically generate this file for you unless you set the `GeneratePkgInfo` parameter to false.

