# Overview

Saker.util is a Java library providing common utility functionality for some use-cases. The library is mainly developed alongside the [saker.build system](root:/saker.build/index.html) and provides required utility functionalities for it.

The Saker.util library can be used separately from the saker.build system. However, if you're already developing against the API of the build system, you don't need to manually add it to your dependencies, as the build system imports all the classes in the library under the `saker.build.thirdparty` package.

The library has a dependency on the API of Saker.rmi, as some types are annotated for proper usage over RMI. 
