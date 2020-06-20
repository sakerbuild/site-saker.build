# App development overview

The development for Apple platforms require you to perform the following build steps:

* Compiling the sources
	* Creating the executables and libraries that you'd like to include in your application.
	* This may include compilations for multiple target architectures.
	* When creating release builds, you'll likely also want to strip the binaries from debugging information.
* Specify the application meta-data
	* Create an *Info.plist* file that contains the essential information about your application. This can be considered as a manifest for the app.
	* You may need to dynamically insert information about the development environment into the property list.
* Create the application bundle
	* Set up the application contents with the resources and binaries that you'd like to contain.
* Sign the application for distribution or testing
	* In order to be able to publish your app, it needs to be signed with an Apple-provided code signing certificate. This may require you to register for a developer account at Apple. (Note that you can develop and test apps without paying the subscription fee.)

The saker.apple package provides build tasks for performing or configuring these build steps. See the following topics for more information.

<div class="doc-table-of-contents">

* [](preset.md)
* [](appbundle.md)
* [](signing.md)

</div>
