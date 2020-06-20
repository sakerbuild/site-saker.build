# Developer macOS

The developer macOS SDK contains information about the operating system that is used to build the application. Similarly to the [Xcode SDK](xcode.md), it is usually used when necessary values need to be inserted into the *Info.plist* file of an application.

The [`saker.apple.sdk.dev_macos()`](/taskdoc/saker.apple.sdk.dev_macos.html) task allows you to get an SDK configuration for the developer macOS:

```sakerscript
# get an Xcode sdk
saker.apple.sdk.dev_macos()
# get a specific version
saker.apple.sdk.dev_macos(Version: "10.11.6")
```
When used with the `Version` parameter, the operating system will be required to have a specific version.

When using [configuration presets](../appdev/preset.md), the Developer macOS SDK will be added to your configuration.