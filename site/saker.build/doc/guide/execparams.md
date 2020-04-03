# Execution parameters

Similarly to the [environment configuration](envconfig.md), you can define arbitrary string key-value pairs that are the *execution user parameters*. These parameters may be used in an use-case dependent manner by the build tasks and other operations that run during build execution.

One example for this is that some repositories can configure themselves differently based on the execution user parameters that are specified for the execution. (E.g. The [saker.nest repository](root:/saker.nest/doc/userguide/configuration.html).)

They can be also retrieved using the [`std.param.exec()`](root:/saker.standard/taskdoc/std.param.exec.html) in the build script.

The execution user parameters can be specified using the [`-U`](/doc/guide/cmdlineref/build.md#-u) command line option.
