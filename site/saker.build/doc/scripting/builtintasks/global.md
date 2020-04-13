# `global()`

The `global()` task can be used to access a global variable. Global variables are accessible from every build script file. They are different from the regular variables known in the language. The variables can be retrieved and assigned.

The result of the `global()` task can be used on the left hand side of an [assignment expression](/doc/scripting/langref/operators/assignment/index.md). 

If other scripting languages than SakerScript is used, global variables may be supported by other languages too. It is dependent on the implementation of those languages.

Global variables have very limited use-case and it is recommended to use the sparingly. One use-case might be to specify global configuration in some entry point of the build scripts, and access them from other files without the need of passing the parameters explicitly. It is recommended to have an unique name for global variables to avoid name collisions, as global variables can be assigned at most once during build execution.

## Parameters

The following parameters are used by the task:

| Parameter 	       	| Description   	|
|----------------------	|---------------	|
| unnamed	| **Required** name of the global variable that is being accessed.	|

## Task result

The reference to the global variable.

## Example

The following example contains two files (`config.build`, and `saker.build`):

`config.build`:
[!embedcode](global.build "language: sakerscript, range-marker-start: #config-build-start, range-marker-end: #config-build-end")

`saker.build`:
[!embedcode](global.build "language: sakerscript, range-marker-start: #saker-build-start, range-marker-end: #saker-build-end")

The above example showcases one use-case for global variables. The `config.build` file is not published to any source control platforms, and contains secret data related to the operation of the associated project. The `saker.build` file is the main entry points for builds and developers use this to build the project.

When the `publish` build target is invoked, the `include(Path: config.build)` global expression includes the `config.build` file. The expressions in it are invoked, and the global variables get assigned with the appropriate values. The `publish` task then encrypts and publishes some arbitrary data, and uses the globally set configuration values to do its work.