# Starting tasks

Starting tasks is available through the [`TaskContext`](/javadoc/saker/build/task/TaskContext.html), and require a task identifier and task factory for the newly started task. The task identifier uniquely identifies a started task instance and allows every other task to retrieve the results for it. The task factory is the implementation of the task that will be newly executed.

In the following example we showcase task usage with the compilation of C++ source files. The user facing task implementation is the following:

[!embedcode](example_startingtasks/src/example/CompileBootstrapperTaskFactory.java "language: java, range-marker-start: //snippet-start,  range-marker-end: //snippet-end, trim-line-whitespace: true")

It may be used by specifying the `Sources` parameter:

```sakerscript
example.compile(Sources: *.cpp)
```

The implementation will discover the files specified by the wildcard and report the appropriate file addition dependency. The path of the files will be iterated over, and a special content descriptor signaling only their presence will be reported for them as dependency.

A new task that executes the compilation of the file is started for every found file. The build system will schedule the started tasks accordingly, and the compilations for each source file will run.

The task identifier for the compilation worker tasks is a [simple data class](example_startingtasks/src/example/WorkerTaskIdentifier.java) that holds the path of the file. The implementation of the compilation worker task is the following:

[!embedcode](example_startingtasks/src/example/CompilationWorkerTaskFactory.java "language: java, range-marker-start: //snippet-start,  range-marker-end: //snippet-end, trim-line-whitespace: true")

The worker implementation locates the file at the given source path location, and reports if as an input dependency. The actual compilation of the file should occur at the location of the associated comment.

## Build scenarios

Let's examine how the task execution works given the following scenarios.

### Simple run

If we have the following files in the working directory: `main.cpp`, `game.cpp`, `resource.txt`, then simply execution the task using the given script:

```sakerscript
example.compile(Sources: *.cpp)
```

Will occur in the following way:

1. The `example.compile` task will be invoked by the build system.
	* It will discover the `main.cpp` and `game.cpp` files to compile, as thos are the ones that match the specified wildcard.
	* One compilation worker task will be started for each of them.
	* The task finishes without a result.
2. The compilation worker tasks for `main.cpp` and `game.cpp` is executed simultaneously.
	* Both task retrieve the [`SakerFile`](/javadoc/saker/build/file/SakerFile.html) instance representing the files.
	* The input dependencies are reported by the worker tasks.
	* The worker tasks compile the file, and finish accordingly.

### Incremental running

Following up on the scenario of [](#simple-run). If you run the build, and run it again without changing any files, the build system will detect that no tasks need to be called, and therefore finishing without doing substantial work.

#### Modifying a file

If you change a compiled source file (e.g. `main.cpp`), then the build system will detect that the appropriate compilation worker task needs to be rerun. It is important to note, that the bootstrapping `example.compile` task will *not* be rerun, as it doesn't report a dependency that relies on the contents of the modified source file. This way, the build system determines that the minimal work it needs to do is to call the compilation worker task for `main.cpp`.

#### Adding a file

If you add a new source file that matches the `*.cpp` wildcard (e.g. `add.cpp`), then the `example.compile` task will be rerun. It will detect the addition of the new source file, and start all tasks for every source file again.

The build system will detect that `main.cpp` and `game.cpp` haven't changed, and the started tasks for them won't be reinvoked. In other words, the build system will do the minimal work for only invoking the compilation worker for the added source file.

#### Removing a file

When you remove a source file (e.g. `game.cpp`), the build system will rerun the `example.compile` bootstrapper task. It will only detect the presence of `main.cpp`, and start the compilation worker for that. 

After the build execution finishes, the build system will detect that the compilation worker task for `game.cpp` hasn't been started by the bootstrapper task. It will delete any output files for that task that resides under the build directory, therefore cleaning up any leftover files. 

In this scenario, no actual compilation worker task is called, but only the bootstrapper task.

## Conclusion

We can see that decoupling various operations into their own task implementation can leverage the incremental support of the build system. By starting subtasks, the build system can optimize the invocations of tasks based on their dependencies, and always doing minimal work when possible.

Appropriately dividing work into tasks can be advantageous as tasks doesn't need to take care of cleaning up after themselves if a corresponding input is removed for an output.
