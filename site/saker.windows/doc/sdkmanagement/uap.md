# UAP/UWP

The Universal Application Platform (Also known as Universal Windows Platform) is a platform for writing applications that run on various Windows devices. The saker.windows package uses this SDK to deal with *.appx* bundles as well as to properly configure the build steps.

The SDK is usually installed at *C:\Program Files (x86)\Windows Kits\10\Platforms\UAP\\&lt;version&gt;* where *&lt;version&gt;* is the version of the platform installation. It contains the necessary information about a given target platform and how applications should be built for it.

You can retrieve an SDK configuration using the [`saker.windows.sdk.uap()`](/taskdoc/saker.windows.sdk.uap.html) task:

```sakerscript
# gets an SDK without a requested version number
saker.windows.sdk.uap()
# gets the SDK with the 10.0.18362.0 version
saker.windows.sdk.uap(10.0.18362.0)
```

The result of the task can be passed to other build tasks that work with SDKs.

