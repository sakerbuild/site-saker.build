# What is an SDK?

From a build system perspective, an SDK is considered to be an immutable installation of resources that can be used by the build in order to perform its tasks.

During the build execution the build tasks can use these resources from an SDK, and perform the necessary tasks with it. Generally, an SDK is **not** part of the project that is being built, but is installed on the machine that runs the build.

Examples for an SDK are:

* A local Java Development Kit installation.
	* You can have multiple JDKs installed, and the build can take advantage of multiple installation. You won't have the JDK installation part of your project, but installed somewhere on your machine.
* A C/C++ compiler toolchain.
	* Similarly to JDK, you install the toolchain on your machine, and the build tasks will invoke the appropriate compiler processes to compile the source files.
* An SDK for a given platform. (E.g. Android SDK)
	* You have the Android SDK installed on your machine, and provide the install location to the appropriate tasks. They can then use this to compile the resources, and produce an appropriate APK.

The saker.sdk.support package attemps to provide standardized access to the SDKs that can be used by multiple build tasks.
