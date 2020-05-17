# SDK management

The Android SDK contains two main components that the build tasks need to properly operate:

* *build-tools*\
  The build-tools contain the executables and libraries that are necessary for resource compilation, packaging, and other operations. 
* *platform* \
  The platforms contain the information about each API release of the Android OS.

Both of these components are contained in the root Android SDK installation. The build tasks will attempt to automatically locate the Android SDK installation, but in some cases you may need to specify them yourself.

The SDK management integrates the [saker.sdk.support](root:/saker.sdk.support/doc/index.html) package for handling various aspects of the Android SDK. 

## SDK location

The Android SDK usually comes with Android Studio, but you can install the [command line tools](https://developer.android.com/studio#command-tools) yourself.

As the default, the build tasks will attempt to locate the SDK installation by using the `ANDROID_HOME` and `ANDROID_SDK_ROOT` environment variables according to [their rules](https://developer.android.com/studio/command-line/variables#android_sdk_root) described on Android Developers.

You can override the Android SDK location by setting the `saker.android.sdk.install.location` [environment user parameter](root:/saker.build/doc/guide/envconfig.html):

```plaintext
-EUsaker.android.sdk.install.location=c:/path/to/android/sdk
```

It will be used to look for the SDK before the mentioned environment variables.

## Build-tools

The [`saker.android.sdk.buildtools()`](/taskdoc/saker.android.sdk.buildtools.html) task can be used to specify the used build-tools version. The task takes a version number as its input, and will select an appropriate build-tools release:

```sakerscript
# uses the latest non-preview version
saker.android.sdk.buildtools()
# uses the version 29.0.3
saker.android.sdk.buildtools(29.0.3)
# uses the version 30.0.0-preview
saker.android.sdk.buildtools(30.0.0-preview)

# uses the latest with major 29
saker.android.sdk.buildtools(29)
# chooses the latest version that has a major
#   at least 25 and at most 29
saker.android.sdk.buildtools("[25, 29]")
```

The input version specification can accept speicifc versions of build-tools, and [version ranges](root:/saker.nest/doc/userguide/versioning.html#version-ranges) as well.

## Platform

The platform SDK can be retrieved using the [`saker.android.sdk.platform()`](/taskdoc/saker.android.sdk.platform.html) task. It takes a version or platform directory name as an input and configures an appropriate release:

```sakerscript
# uses the latest non-preview version
saker.android.sdk.platform()
# uses the platform with API level 29
saker.android.sdk.platform(29)
# (same as above)
saker.android.sdk.platform(android-29)
# uses the R preview platform
saker.android.sdk.platform(android-R)
```

## SDK names

The mentioned SDKs have a specific names associated with them. The build-tools use the `AndroidBuildTools` identifier, and the platform SDK use the `AndroidPlatform` SDK name. You need to use these when passing them as the input of `SDKs` parameters:

```sakerscript
saker.android.aapt2.compile(
	# ...
	SDKs: {
		AndroidBuildTools: saker.android.sdk.buildtools(29)
		AndroidPlatform: saker.android.sdk.platform(29)
	}
)
```
