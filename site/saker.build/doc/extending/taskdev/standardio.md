# Standard I/O

Tasks can read from the standard input, and write to the standard output of the build execution. These I/O streams are shared with the other concurrent task executions, and they are handled specially by the build system.

The build system also supports printing to the standard output on a per-line basis, which will be printed out again if the task is not run due to no changes in the next build execution.

## I/O handling in the build system

The I/O streams of the build execution serves for displaying character data to the user about the build task execution. These may contain any task related information that can help the user follow the progess of a task, or just help them debug it. The streams should not be used for writing binary (i.e. non-displayable characters) data.

The tasks are provided with a standard output, error, and input streams.

The standard output stream is accessible via [`TaskContext.getStandardOut()`](/javadoc/saker/build/task/TaskContext.html#getStandardOut--) function. It is buffered internally on a per-line basis, and automatically written out to the actual output when appropriate. This means that the task context will print out the lines when it sees most fit, and will not print out partially finished lines. The printed lines may be interlaced with other outputs from concurrently running tasks.

The standard error stream is accessible via [`TaskContext.getStandardErr()`](/javadoc/saker/build/task/TaskContext.html#getStandardErr--) function. It is buffered internally by the task context, and only written out in whole after the associated task finishes. This is in order to avoid interlacing of lines like in the standard output, and to provide a cleaner display of errors for the user.

The standard input stream is accessible via [`TaskContext.getStandardIn()`](/javadoc/saker/build/task/TaskContext.html#getStandardIn--) function. This stream can be used to read input from the developer that is running the build execution. It is not buffered, and the I/O lock must be acquired before issuing any reading method calls.   

## I/O locking

The I/O lock of the build execution is used to get a build execution-wide exclusive lock to access all of the I/O streams of the execution. The locking doesn't happen on a per-thread basis, but on a per-task basis.

When the I/O lock is acquired, only the acquiring task can write and read from the standard streams. Other tasks will need to wait for the release of the lock to use the standard streams.

The lock can be acquired via the `TaskContext.acquireStandardIOLock()` function, and must be released using the `TaskContext.releaseStandardIOLock()` function. If the lock remains acquired after the task finished, the build system will release it automatically, however, it is strongly recommeded to do it manually nonetheless.

Acquiring the lock can be useful when the task developer wants to write out contents to the output without having them broken up by possible concurrent writes from other tasks. It is also required when the developers wants to read from the input streams.

When the lock is released, any partially written out lines to the standard output will be appended with a new line.

## Standard output

The characters written to the standard output is buffered, and written out automatically to the actual output at the discretion of the build system. Writing to the standard output is possible using the following ways:

* Using [`TaskContext.getStandardOut()`](/javadoc/saker/build/task/TaskContext.html#getStandardOut--).
	* Character bytes can be written to the returned stream.
	* The bytes written to this is **not replayed** by the build system in case of no incremental changes.
* Using [`TaskContext.println()`](/javadoc/saker/build/task/TaskContext.html#println-java.lang.String-).
	* Printing a line of character data to the output.
	* These lines will be **printed again** the the output stream if the task doesn't run in a build execution due to no incremental changes.
* Using `System.out`.
	* The default Java standard output is redirected by the build system to handle writing to the task related output stream. Tasks can use this to write out characters.
	* This is basically the same as using the `TaskContext.getStandardOut()` stream.
	* Tasks shouldn't rely on this stream, as this might be hijacked by other agents.
* Using [`SakerLog`](/javadoc/saker/build/runtime/execution/SakerLog.html) class.
	* The logging class provided by the build system can be used to print out information in a formatted way.

Any unflushed data written to the output will be flushed when the task finishes.

Tasks may want to acquire the I/O lock to avoid interlaced outputs with other concurrent tasks.

Note that using the above writing mechanisms **will not** stall your calling thread by trying to wait for the I/O lock. If the lock is not available, the build system will not wait for acquiring it and printing out the characters, but buffer them internally.

### Display identifier

The task context provides the facility of prepending the lines written to the standard output via a custom display identifier. An arbitrary identifier may be specified to prepend all lines written to the output.

The [`TaskContext.setStandardOutDisplayIdentifier(String)`](/javadoc/saker/build/task/TaskContext.html#setStandardOutDisplayIdentifier-java.lang.String-) can be used to set this identifier, and after doing so, the lines will be prepended with then in the `[identifier]` format.

The output of the following:

```java
taskcontext.println("hello");
taskcontext.setStandardOutDisplayIdentifier("my-taskid");
taskcontext.println("world");
```

Will be:

```
hello
[my-taskid]world
```

This functionality helps the reader separate the outputs of different tasks.

## Standard error

The characters written to the standard error is buffered, and they are written out in whole after the task finishes its execution.
Writing to the standard output is possible using the following ways:

* Using [`TaskContext.getStandardErr()`](/javadoc/saker/build/task/TaskContext.html#getStandardErr--).
	* Character bytes can be written to the returned stream.
* Using `System.err`.
	* The default Java standard error is redirected by the build system to handle writing to the task related output stream. Tasks can use this to write out characters.
	* This is basically the same as using the [`TaskContext.getStandardErr()`](/javadoc/saker/build/task/TaskContext.html#getStandardErr--) stream.
	* Tasks shouldn't rely on this stream, as this might be hijacked by other agents.

Acquiring the standard I/O lock has no effect on the writing to the task standard error.

## Standard input

The standard input can be used to read various input data from the user interactively. This is not always available, and tasks generally shouldn't rely on the standard input.

The standard input can be used via the following:

* Using `TaskContext.getStandardIn()`.
	* Can be used to read bytes from the returned stream.
* Using `System.in`.
	* The default Java standard input is redirected by the build system to handle input reading of tasks.
	* This is basically the same as using the `TaskContext.getStandardIn()` stream.
	* Tasks shouldn't rely on this stream, as this might be hijacked by other agents.

In both above cases, the standard I/O lock **must** be acquired before attempting to read from a stream. Not doing so will result in an exception.

When any reading function call is issued, the build system will flush any buffered standard output data, and then issue the reading call to the shared input stream. This usually results in the user being prompted in the console to input some characters. While the reading is being called, no other outputs can be written to the output streams.

In general tasks should lock the I/O, write out some information about what the prompt is about, and then issue the read calls to the standard input stream.

Note that the standard input will not always be available. It might be that the build execution is running without a developer sitting in front of it, therefore no input will be available. (E.g. Continuous integration builds)

### Reading secrets

The build system supports reading secret input from the developer. This usually entails that the developer will be prompted in the console in a way that entering the characters will not be echoed back. However, the secret reading might be implemented in different ways, like in dialogs, or a secret database.

Reading secrets can be done by issuing read requests on the object returned by [`TaskContext.getSecretReader()`](/javadoc/saker/build/task/TaskContext.html#getSecretReader--).

The standard I/O lock is not required to be acquired for reading secrets. If necessary, the build system will automatically acquire and release it for the duration of the reading.
