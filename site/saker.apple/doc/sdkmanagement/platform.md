# Platform SDKs

Platform SDKs are the ones that are the target of your application. A platform SDK contains resources that allow you to compile your applications for a given platform. These are usually the libraries, header files, frameworks and other resources necessary.

Platform SDKs are the following:

* iPhoneOS, iPhoneSimulator
	* These SDKs target the iPhone or its associated simulator.
* MacOSX (macOS)
	* For applications running on macOS.
* AppleTVOS, AppleTVSimulator
	* To create applications for Apple TV.
* watchOS, watchSimulator
	* The SDKs to develop for Apple Watch or its corresponding simulator.

The configuration for these SDKs can be retrieved using the [`saker.apple.sdk.platform()`](/taskdoc/saker.apple.sdk.platform.html) task:

```sakerscript
# get an SDK for iPhoneOS
saker.apple.sdk.platform(iphoneos)
# get an SDK for iPhoneOS 10.2
saker.apple.sdk.platform(iphoneos, Version: "10.2")
```

You can use the `Version` parameter to get an SDK for a specific version. The platform names are interpreted in a case-insensitive way.

When using [configuration presets](../appdev/preset.md), the associated platform SDK will be added to your configuration.