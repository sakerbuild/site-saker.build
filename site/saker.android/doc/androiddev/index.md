# Introduction

The saker.android package allows you to develop Android applications using the [saker.build system](root:/saker.build/index.html). It provides build tasks that allow you to invoke the appropriate tools to create the application.

The tools that you'll be working with are the following:

[**AAPT2**](https://developer.android.com/studio/command-line/aapt2) is used to compile and package the resources that your app uses.

[**D8**](https://developer.android.com/studio/command-line/d8) can convert your compiled Java class files to DEX bytecode. This is necessary as Android uses a different bytecode format for code representation.

[**Apksigner**](https://developer.android.com/studio/command-line/apksigner) is used to digitally sign your applications. It is required so you can install, test, and publish your app.

[**AIDL**](https://developer.android.com/guide/components/aidl) is used if you want to implement interprocess communication between various processes on Android.

The package also supports resolving AAR dependencies and configuring the classpath for them automatically.
