# Environment configuration

Each build execution runs inside a build environment. The build environments can be configured using *environment user parameters*.

The environment user parameters are arbitrary string key-value pairs which can be set for the build environment. These parameters may be interpreted in an use-case dependent manner by the build tasks or any other operations that run inside the build environment.

They can be also retrieved using the [`std.param.env()`](root:/saker.standard/taskdoc/std.param.env.html) in the build script.

Generally, the environment user parameters can be used to specify locations of installed toolchains, SDKs, and other resources that are considered to be static and can be used by build tasks.

The environment user parameters can be specified using the [`-EU`](/doc/guide/cmdlineref/build.md#-eu) command line option.

## Storage directory

The build environments are also configured with a storage directory. This storage directory can be used by the build environment to store various files that are not directly related to building projects, but have a longer lifetime. One such example is to store contents of loaded task repositories and various classpath files from where Java classes can be loaded. The loaded repositories can also store files there.

The storage directory doesn't contain configuration related data.
