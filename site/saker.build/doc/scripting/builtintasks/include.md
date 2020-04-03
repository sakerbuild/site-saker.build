# `include()`

The `include()` task can be used to include other build targets in the current build execution.

## Parameters

The following parameters are used by the task:

| Parameter 	       	| Description   	|
|----------------------	|---------------	|
| `Target`, unnamed	| *Optional* name of the build target to invoke. If not specified, a default one is chosen.	|
| `Path`			| *Optional* path to the build file that contains the invoked target. The file of the current build script is considered when not specified.	|
| `WorkingDirectory`| *Optional* path to the working directory for the build target. The parent directory of the target build file is used when not specified.	|
| other				| *Optional* other parameters to pass to the invoked build target. The parameters may have any arbitrary names.	|

Both the `Target` and `Path` parameters are optional, but at least one of them must be specified. \
If the `Path` parameter is missing, the target build file is considered to be the same as the one calling the `include()` task. \
If the `Target` parameter is missing, a default build target will be chosen based on the structure of the target build file:

* If there is only a single build target in the file, that one is chosen.
* If the target file contains a build target named `build`, that one is used.
* If there are no build targets in the target file or failed to determine based on the above rules, an exception is thrown.

Keep in mind that if there are no explicit build targets, but there are [global expressions](/doc/scripting/langref/sourcefile/index.md#global-expressions) in your file, then an implicit build target will be defined for you.

Note that build directory related parameter is intentionally left out. All included targets will be invoked with having the execution build directory as their build directories.

## Task result

The result of the build target invocation. The output parameters of the invoked target is accessible using the [`[subscript]` operator](/doc/scripting/langref/operators/subscript/index.md).

## Example

The following example contains two files (`saker.build`, and `library.build`):

`saker.build`:
[!embedcode](include.build "language: sakerscript, range-marker-start: #saker-build-start, range-marker-end: #saker-build-end")

`library.build`:
[!embedcode](include.build "language: sakerscript, range-marker-start: #library-build-start, range-marker-end: #library-build-end")

In the above example we can see that the compilation of a library and the user of that library have been decoupled into different build files. They include each other when the result of the given result is needed.
