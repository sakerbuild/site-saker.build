# File mirroring

Related: [](/doc/guide/pathconfiguration.md#mirror-directory).

It is a common scenario for task implementations to delegate their works to external processes. One example for this is calling a C/C++ compiler for compiling a source file.

In such use-cases it is necessary to make a file from the in-memory hierarchy available on the local file system, so they can be passed as an input to the external process.

To support this use-case the build execution can be configured to have a mirror directory where tasks can temporarily export files to. The external processes can also write their results to this directory, while the tasks can add the result files to the in-memory hierarchy.

The build system provides the mirror functionality through the [`ExecutionContext`](/javadoc/saker/build/runtime/execution/ExecutionContext.html) object. Simple example for calling an external process with a file as the input:

[!embedcode](example_filemirror/src/example/FileMirroringTaskFactory.java "language: java, range-marker-start: //snippet-start,  range-marker-end: //snippet-end, trim-line-whitespace: true")

The above example takes a file path parameter from the user and runs the `myexe` process with it as the argument. The file is mirrored to the local file system before calling the external process. As the result of the mirroring, a `Path` instance is returned which is the location where the file was actually mirrored. 

The `ProcessBuilder` is used to construct, start, and wait for the external process. After finishing, the file is reported as an input dependency to the task.

**Important** to note that the mirroring functionality may return a path that is the actual path where the file would be synchronized. This means that if the file represented by the associated [`SakerFile`](/javadoc/saker/build/file/SakerFile.html) object corresponds to a file on the local file system, then the mirroring may not actually mirror the file to the mirror directory, but simply just synchronize the file itself and return the local file path accordingly.

Keeping the above in mind, any external processes which access the mirrored files must not modify them, but put their results (if any) to another predefined location. If an external modification of a file cannot be avoided, the `TaskContext.invalidate` method provides an opportunity for a task to notify the build system about it.

## Output files

The invoked external processes may create their own output files which need to be added back to the file hierarchy of the build execution. This can be done by creating a [`SakerFile`](/javadoc/saker/build/file/SakerFile.html) which have its contents backed by the file that was created by the process. The `TaskExecutionUtilities` provides methods to create such files by calling `createProviderPathFile`. It (and its overloaded pairs) is a generalized method that allows to create such files not only for the local file system but for others as well.

The output files may be written to mirror paths which can then easily be imported to the build system. A simple example that make use of this:

[!embedcode](example_filemirror/src/example/FileMirroringOutputTaskFactory.java "language: java, range-marker-start: //snippet-start,  range-marker-end: //snippet-end, trim-line-whitespace: true")

The above example is similar to the previous one, however, the process we invoke also creates an output file that we need to add back to the build system hierarchy. The initialization is similar, and we're going to put the result file in the build directory with the name of `outfile.bin`.

We use the `toMirrorPath` function to determine the location where the process can write the output file. The method converts the path of the build directory to the mirror path that corresponds to it. We pass it to as the process argument to tell it to write the outputs to that given file.

The task utilities `createProviderPathFile` method is used to create a [`SakerFile`](/javadoc/saker/build/file/SakerFile.html) representation of the resulting file, and then that will be added back to the build directory accordingly. At the end, the file is synchronized to ensure that it is persisted to the appropriate location, and the files are reported to the build system as dependencies.
