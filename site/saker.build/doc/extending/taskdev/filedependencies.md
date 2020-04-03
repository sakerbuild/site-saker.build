# File dependencies

Build tasks are required to report their dependencies on input files that they've used to produce the outputs, and the output files they've created during their computations. This ensures that the build system can properly determine the changes for incremental builds, and therefore reinvoke the task when any of them changed.

## Input dependency

The below example task simply returns the contents of a file for a given path:

[!embedcode](example_inputfile/src/example/FileContentsTaskFactory.java "language: java, range-marker-start: //snippet-start,  range-marker-end: //snippet-end, trim-line-whitespace: true")

If we setup an environment that contains a `file.txt` named file in the working directory, with `abcd123` as its contents, then the following will print out exactly that:

```sakerscript
$contents = example.filecontents(Path: file.txt)
print($contents)
```

If we run the build without any changes, it will print out the contents again without rerunning our task. However, if we modify the contents of `file.txt` to `xyz456`, and rerun the build, our task will rerun accordingly, because a dependency for the input file was reported, and the build system can detect the file changes.

In the task implementation we used the task utilities to resolve a file at a given path. The task utilities is an object that provides various convenience functions for dealing with tasks.

```java
SakerFile file = taskcontext.getTaskUtilities().resolveAtPath(Path);
if (file == null) {
	throw new NoSuchFileException(Path.toString());
}
```

If the file is not found at the given path, `null` is returned, signaling its absence. We throw an exception in order to signal the user the failure. The build task in this case will not complete successfully, and if the build is rerun, it will be invoked again.

The build tasks are required to report input (and output) dependencies using the following (and output correspondent) method:

```java
taskcontext.reportInputFileDependency(null, Path, file.getContentDescriptor());
```

The `null` argument corresponds to the [tag](#dependency-tags) of the dependency. The dependency is recorded as a path-content descriptor pair. When a new build execution is started, these pairs will be examined against the current state of the files, and if any changes are detected, the task will be rerun. Reporting an input file dependency is basically saying that *"I expect the file at this path to have this contents."*.

## Output dependency

The below task calculates the sum of two integer parameters and writes them to a file under the build directory:

[!embedcode](example_outputfile/src/example/SumOutputFileTaskFactory.java "language: java, range-marker-start: //snippet-start,  range-marker-end: //snippet-end, trim-line-whitespace: true")

When used with the following build script:

```sakerscript
example.sum.file(Left: 4, Right: 6)
```

A file will be created with the path `example.sum.file/sum.txt` under the build directory with the contents of `10`. 

The file dependencies are reported in similar way to the input dependency reporting. If anybody modifies the created `sum.txt`, and the build executions is rerun, then our task will be rerun as well, as the build system detected that one of our output dependencies have changed. As a result the task will recreate the `sum.txt` again. Reporting an output file dependency is basically saying that *"I've put a file to this path with these contents"*.

We've put the file in the in-memory file tree, and we need to synchronize the contents as well, using the following call:

```java
outfile.synchronize();
```

This ensures that the file will be persisted to the appropriate disk location, and will not only exist in the memory.\
*The build system will not automatically synchronize the output files.*

## Dependency tags

Dependency tags are arbitrary objects which can be used by tasks to link data to a reported dependency. These objects are not interpreted by the build system in any way.

Their use-case is that when a task is rerun due to file changes, the tag will be available for the task to retrieve for the file delta. Based on the tag, the task can determine the nature of the change and compute more easily the appropriate operations that needs to be taken.

An examples for this:

A task that compiles C++ sources reports `.cpp` source files with the `SOURCE_FILE` tag, and `.h` header files with `HEADER_FILE` tag.\
If the user changes a source file, the task will receive a file change delta with the `SOURCE_FILE` tag. It can the determine that nothing else needs to be done, but compile that changed source file.\
If the user changes a header file, the task will receive a file change dlta with the `HEADER_FILE` tag. It determines that the given header was changed, and recompiles all source files which include this header file.

If the tag functionality wasn't used, the task would need to examine other properties of the changed files and determine the nature of the changes in a more costly way.

See also: [](incrementaldeltas.md).
