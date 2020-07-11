# Windows Kits

The *Windows Kits* SDK is what is considered **the** Windows SDK. It provides many features to develop for the Windows platform(s).

It generally contains the necessary libraries, header files, tools, and other resources necessary for developing Windows applications. It is usually installed under the *C:\Program Files (x86)\Windows Kits\10* location.\
Depending on your installation, it may contain various tools for different platforms alongside.

The saker.windows package provides configuration for representing this as an SDK and provides access to the tools and resources inside it. The SDKs are differentiated using a version number that the installation has.

The version of the installation depends on the directories in which the resources reside. E.g. if an executable is in the *Windows Kits\10\bin\10.0.18362.0\x64* directory, then the SDK is considered to have the *10.0.18362.0* version.

You can retrieve an SDK configuration using the [`saker.windows.sdk.windowskits()`](/taskdoc/saker.windows.sdk.windowskits.html) task:

```sakerscript
# gets an SDK without a requested version number
saker.windows.sdk.windowskits()
# gets the SDK with the 10.0.18362.0 version
saker.windows.sdk.windowskits(10.0.18362.0)
```

The result of the task can be passed to other build tasks that work with SDKs.

### Windows kits environment parameter

The `saker.windows.sdk.windowskits.install.location.<version>` build environment user parameter can be used to specify the location of a Windows Kits installation.

For example:

```plaintext
saker.windows.sdk.windowskits.install.location.10.0.18362.0=c:\Program Files (x86)\Windows Kits\10
```

Note that you must specify the installation directory path that contains the `Include`, `Lib` and other directories instead of any of the subdirectories.

