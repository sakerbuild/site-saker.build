# Unsupported features

When compared to [Gradle](https://gradle.org) (the official build system for Android), saker.android still lacks some functionality. While it provides some advantages such as [faster builds](perfcomparison.md) and better Java compilation support, you should also know that you may need to give more attention to edge cases.

On this page we'd like to enumerate some of the known missing features for the saker.android package. Note that this just means that these features are not implemented *currently*. They may (and probably will) be added in future versions.

* Kotlin
	* Kotlin is greatly supported by the Android ecosystem, however, saker.build doesn't have a released solution for compiling it yet.
* ADB support
	* The saker.android package doesn't provide build tasks for communicating with ADB.
* [Merging multiple manifest files](https://developer.android.com/studio/build/manifest-merge)
	* You will need to take special care when using AAR libraries.
* [App bundles](https://developer.android.com/platform/technology/app-bundle)
	* No build tasks are implemented yet for creating them.
* Proguard
	* No build tasks are implemented yet for running proguard on your app code.
* Data binding 
	* Support for the [data binding library](https://developer.android.com/topic/libraries/data-binding) is not verified for the saker.android package. You may be able to get it working, but we haven't tried it.
