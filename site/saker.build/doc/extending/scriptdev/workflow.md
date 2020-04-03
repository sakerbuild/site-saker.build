# Development workflow

The development of a scripting language requires some additional care in some use-cases. In general, you can develop the language like you would with any other Java/JVM based project. We strongly recommend testing of your language with unit, integration, and sometimes manual testing. See [](/doc/extending/testing/index.md) for integration testing guide. 

A common use-case is that when developing a language, one will decide to use it in a project right away. This is completely acceptable, however the build system needs to be notified about the implementational changes of the scripting language. When you update the language implementation and rerun a build of a project that uses the language, you'd expect that the execution uses the updated language implementation. However, this might not be always the case.

First of all, when using the build daemon, the build system caches the class path of the language implementations and keeps the loaded script providers in memory to avoid always loading the class path. In order to overcome this issue, if you expect the language implementation to be changed, don't use daemons, or clear the cached data of a daemon. (This includes IDE usage as well. See Window > Saker.build > Reload plugin environment option in Eclipse)

Secondly, even if you don't use daemon, you need to represent the changes made in the language tasks that they've been changed. This is not straightforward, as when the build system loads the classes, it will likely load the new implementation classes with the data from the old implementation. In this case the build system will not notice the changes, as it will not have the reference to the old classes to compare with the new.\
In order to solve this, you can do the following (any of them is sufficient):

1. Modify the [script accessor key](structure.md#accessor-key) for each exported implementation.
	* This will cause the build system to fail to load the old implementation of the classes, and therefore recognize that the implementation have been changed.
	* A solution for this is to configure the build to include the build time or otherwise transient data in the script accessor key.
2. Clean the project that uses your language.
	* This results in the build system cleanly building your project and using the most up-to-date imeplementations of everything.

In the end, if you experience any unexpected behaviour after updating the language implementation, you should stop all daemons, clean the projects, and run the build again to see if the behaviour still persists.
