# Xcode

The Xcode SDK contains information related to the build environment that is used to create the application. It is usually used when necessary values need to be inserted into the *Info.plist* file of an application.

The [`saker.apple.sdk.xcode()`](/taskdoc/saker.apple.sdk.xcode.html) task allows you to get an SDK configuration for an Xcode installation:

```sakerscript
# get an Xcode sdk
saker.apple.sdk.xcode()
# get a specific version
saker.apple.sdk.xcode(Version: "8.2.1")
```

When used with the `Version` parameter, the Xcode SDK will be required to have a specific version.

When using [configuration presets](../appdev/preset.md), the Xcode SDK will be added to your configuration.