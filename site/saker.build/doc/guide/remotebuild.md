# Remote build execution

Remote build execution is the practice of invoking a build in a different environment/machine than your own. This usually entails that the machine that is building your code is accessed through the network or other measures.

Remote build is useful in many cases such as:

* Using a more resourceful computer to build your project.
	* Delegating your builds to a faster machine will result in more performant builds. This is beneficiary when you're limited by battery, or can't access a high-end computer to edit some code.
	* In case of enterprise use, when a high-end server is available, building the software on it can greatly reduce the build times for your team.
* Delegating the builds to machines where the tooling is available.
	* It can be hard if even possible to set up a build environment for cross-platform compilations. It may be easier to compile your code directly on the target platform. 
	* This is especially prevalent when developing for Apple operating systems (iOS, macOS), but you still prefer a different operating system as your daily driver. Editing your code on Windows/Linux/etc..., and then delegating the builds to your macOS machine can be transparently done by saker.build.

Configuring a build for remote execution also enables you to use files in your build from different machines. The file system mounting configuration allows you to setup the paths for your build in a way that can transparently access files from multiple different computers.

This has the advantage that you don't need to have all the project files on your computer. This functionality probably has fewer use-cases, but it lets you configure the build in more ways nonetheless.
