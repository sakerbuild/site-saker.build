[!section](releasearchive.md)

# Installation

The main use-case for saker.nest is to be used with the [saker.build system](root:/saker.build/index.html) as a task repository. It is automatically included in build executions, unless disabled. If you indent to used the repository with saker.build, then you don't need to do any installation. In other cases, see the [saker.build installation guide](root:/saker.build/doc/installation.html) as well.

If you want to use a different version of saker.nest with the build system than the included default, then you can do any of the following:

* Use the download link directly with the build execution. Determine the download URL for the version to be used, and pass it as a parameter to the [`-repo`](root:/saker.build/doc/guide/cmdlineref/build.html#-repository) command line option or modify the IDE configuration accordingly. Saker.build will automatically download the repository release for the build.
* [Download](https://api.nest.saker.build/bundle/download/saker.nest-v#MACRO_VERSION_SAKER_NEST) the saker.nest version release of interest and add it as a repository to the build execution. Use the [`-repo`](root:/saker.build/doc/guide/cmdlineref/build.html#-repository) command line option or modify the IDE configuration accordingly.

If you specify your own release, it is recommended to disable the automatic inclusion of the repository by using the [`-repo-no-nest`](root:/saker.build/doc/guide/cmdlineref/build.html#-repository-no-nest) command line option. (This is only for command line invocation, you can just simply remove the default repository configuration in the IDE.)

If you provide the repository release by yourself, you may need to do additional configuration of the local repository if used.

## Download

You can download the latest saker.nest release using [this link](https://api.nest.saker.build/bundle/download/saker.nest-v#MACRO_VERSION_SAKER_NEST).

For older releases, see the [release archive](releasearchive.md).
