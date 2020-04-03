# Basics

The saker.maven.support package defines the following operations:

* Artifact dependency resolution. The [`saker.maven.resolve()`](/taskdoc/saker.maven.resolve.html) task allows you to specify dependencies that should be resolved using the Maven dependency resolution algorithm. The task can take artifact coordinates, dependency declarations, or a [`pom.xml`](https://maven.apache.org/pom.html) as the base of dependency resolution.
	* The result of the dependency resolution is a set of artifacts.
* Artifact localization. The [`saker.maven.localize()`](/taskdoc/saker.maven.localize.html) task will take one or more artifact coordinates and make them available on the local machine. This usually entails downloading artifacts through the network and caching them in the local repository. Note that the *download* operation defined by this package shouldn't be confused with the artifact downloading over the network.
* Artifact downloading. The [`saker.maven.download()`](/taskdoc/saker.maven.download.html) task will *download* the artifact files in the sense of making them available for the build execution in the build file system. This usually entails that the artifacts will be copied to a location in the build directory.

The artifact localization and downloading operations was separated in order for the developers to be able to choose the most efficient operation for their builds. E.g. if you don't need to do complex operations with the artifacts, it is usually enough to just localize them in the local repository.

In any of the tasks this package defines, the repository configurations can be done the same way, using the `Configuration` parameter.

