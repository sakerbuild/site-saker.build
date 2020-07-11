# Roadmap

This document contains the roadmap for the development of the saker.build system. It is related only to build system features, other possible additions and doesn't include information for other packages. For roadmaps concerning language support and other build tasks see the associated packages.

This document contains the proposed features, improvements, and crucial bugfixes for the saker.build system. The targeted goals doesn't specify a release date, as due to the limited maintainer development resources, we currently can't estimate exact release dates.

The specified features, improvement and bugfixes will ship as they are implemented. Progress can bee seen in the associated GitHub issues. The priority of a goal is determined on a case-by-case basis.

See also: [saker.nest roadmap](root:/saker.nest/doc/roadmap.html)

#### Build cache

The [build cache](/doc/guide/buildcache.md) functionality for the build system is partially complete. A build cache server needs to be implemented that allows retrieving and publishing task results.

Optimally, the build cache shoulb be configureable to only allow publishing from specified sources, e.g. from Continuous Integration, and should allow publishing from local dev builds.

#### Standard task improvements

Based on the use-cases and feedback for the build system, the [saker.standard](root:/saker.standard/index.html) packages should be improved to support commonly occurring scenarios. This goal should be based on user feedback.

#### Build trace

**Update**: Basic implementation of the build trace is complete. Additional improvements are still to be developed. See [](guide/buildtrace.md).

Build tracing feature should be added to saker.build. A build trace is a recording of the build process. It allows the developer to visualize and examine the build process.

The build trace should provide access to the following related to build tasks:

* Tasks visualized on a timeline chart.
* Task dependencies on other tasks.
* Task environment and execution property dependencies.
* Task inner task executions.
* Task delta and incrementality information.
* Task remote dispatching information.
	* Where the tasks were dispatched. Why they were dispatched there, and why not if not.
* Task file dependency information.	
* Report possible file dependency conflicts.
* Other task related meta-datas.

The build trace should consist of two parts. First, a generated data that is produced by the build execution. Second, HTML pages that can be used to visualize the mentioned data. The build trace should be viewable offline.

The build trace recording should be non-intrusive to the build execution.

#### Clang & GCC support

**Update**: Clang support is under development in the [saker.clang](https://github.com/sakerbuild/saker.clang) repository.

The [saker.msvc](root:/saker.msvc/index.html) package implements features for the MSVC C++ toolchain. Build task implementations for the `clang` and `gcc` compilers should be implemented as well.

Based on the saker.msvc build task implementations, modifying them to support the above compilers shouldn't be a hard task, but is not straightforward. The implementations should be published as a new package.

Common parts for C/C++ support may be exported to newly introduced packages if appropriate.

#### Android support

**Update**: Android support is under development in the [saker.android](https://github.com/sakerbuild/saker.android) repository.

Support for using saker.build to develop for Android should be added. The implementation may be gradual, meaning that at start the build tasks should support creating basic Android applcations, and later it can be extended for more complex use-cases.

#### iOS & macOS support

**Update**: Basic support for Apple platforms are introduced by the [saker.apple](https://github.com/sakerbuild/saker.apple) package.

Support for using saker.build to develop for iOS & macOS should be added. The implementation may be gradual, meaning that at start the build tasks should support creating basic iOS & macOS applcations, and later it can be extended for more complex use-cases.

#### .NET, C#, and related technologies

It shall be examined how developing for .NET, C# and related technologies can be integrated with the saker.build system. This goal is to be split up once the goal progresses.

#### IDE plugin support

**Update 2**: The first release of the IntelliJ plugin is available. See [](intellijplugin.md) for install instructions.

**Update**: The IntelliJ plugin is under development. See the [saker.build.ide.intellij](https://github.com/sakerbuild/saker.build.ide.intellij) repository.

Plugin support for other Integrated Development Environments should be added (other than [Eclipse](eclipseplugin.md)). It should be examined how popular IDEs can be integrated with, its feasibility, and possibility of the implementation.

Also related: [language-server-protocol](https://github.com/microsoft/language-server-protocol/) support for scripting languages. [Semantic highlight](https://github.com/Microsoft/language-server-protocol/issues/18) would be necessary for proper support. (Also: [#33](https://github.com/microsoft/language-server-protocol/issues/33))
