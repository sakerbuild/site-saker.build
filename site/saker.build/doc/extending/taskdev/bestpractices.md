# Best practices

This article contains best practices which we recommend following when implementing your own tasks.

## Storing execution state

When handling incremental builds, tasks will likely need to store some state related data between executions. The build system supports this by making tasks able to store arbitrary key-value data pairs which are only visible to them.

The `TaskContext.setTaskOutput` function allows tasks to store implementation related data which can be queried using the `TaskContext.getPreviousTaskOutput(Object, Class<T>)` function. If the function returns non-null, then the tasks can work based on the state from the previous execution and the deltas which cause the task to be invoked.


```java
public class MyTaskState { /* ... */ }

MyTaskState state = taskcontext.getPreviousTaskOutput("state", MyTaskState.class);
MyTaskState newstate;
if (state != null) {
	// execute the task in an incremental way
	newstate = new MyTaskState(/* ... */);
} else {
	// no previous run, do a clean execution of the task
	newstate = new MyTaskState(/* ... */);
}
taskcontext.setTaskOutput("state", newstate);
```

Although these functions are named as `TaskOutput`, any values that you set will not be externally visible to other tasks. You can also retrieve the object returned from the previous `Task.run(TaskContext)` execution by using `TaskContext.getPreviousTaskOutput(Class<T>)`.

## Clean execution

When your task is executed for the first time, or in case of some configuration changes, it might need to do a clean execution. This entails that there is no previous state or data available for it to make the execution incremental, and therefore build cleanly.

In these cases it is important to remove possible stale resources from output directories. Like in the [above case](#storing-execution-state) where no previous state is available, it is necessary to clean the output directories.

```java
SakerDirectory outputdir = /* ... */;
boolean shouldcleanlybuild = /* is there any reuseable state? */;
if (shouldcleanlybuild) {
	outputdir.clear();
}
// execute the build ...
outputdir.synchronize();
```

In the above scenario we clear the output directory where we would place the resulting files of our task. This is strongly recommended as when some configuration changes occur, there may be leftover files from unrelated operations which should not be part of the results of our task.

See also: [](reproducible.md#delete-stale-outputs).

## Task workflow

It is important to keep the [recommended workflow](incrementaldeltas.md#incremental-workflow) of task execution when implementing one. We recommend doing the following to adhere this requirement:

1. Parse any parameters of the task (if any).
	* This entails that the user provided parameters should be parsed and converted into an internal representation.
	* This also means that all the tasks which may be an input for our task is waited for.
	* This phase can be important, as in some cases if parameter contents are accessed after the file deltas have been calculated, an implicit dependency may be added for the associated task. Using the `@SakerInput` annotations may cause unexpected behaviour if the parameters are accessed later.
2. Examine the state of the task and determine any incremental work to be done.
	* If the task is not being run for the first time, it may reuse the state of a previous execution. If it determines to do so, the task should determine what work it should actually do to make the output up to date.
3. Execute the work that needs to be done.
	* Based on the previously parsed configuration and incremental state, execute the work of the task.

A simplified example for the above is the following:

```java
@SakerInput
public Map<String, String> Inputs;

// convert the input map to our internal representation
//     all the useful entries are accessed
Map<String, String> actualinputs = new TreeMap<>(this.Inputs);
MyTaskState prevstate = taskcontext.getPreviousTaskOutput("state", MyTaskState.class);
if (prevstate != null) {
	// do incremental build
	//    e.g. examine the file deltas
	TaskFileDeltas filedeltas = taskcontext.getFileDeltas();
	// ...
} else {
	// do a clean build for the task
}
```

## Worker tasks

When the build script is modified, or in some other cases your task may get reinvoked even though no real configuration change has happened. The following provides an example:

```sakerscript
example.task(Input: 123)
```

When modified to:

```sakerscript
$var = 123
example.task(Input: $var)
```

The `example.task` will be reinvoked even though the value of `Input` parameter haven't semantically changed. This is because the SakerScript implementation will assign different task identifiers to the invoked tasks therefore the two will be different.

The above scenario can result in too many unnecessary reinvokactions therefore unnecessary work to be done and delaying the build execution.

In order to avoid this, task implementations are recommended to use worker tasks, which contain the exact configuration in their task factory. A minimal example is the following:

The front-end task (that gets invoked by the script) is as follows:

[!embedcode](example_workertask/src/example/ExampleTaskFactory.java "language: java, range-marker-start: //snippet-start,  range-marker-end: //snippet-end, trim-line-whitespace: true")

We create a task identifier for our worker task and start it accordingly through the task context. The [structured task result](taskresults.md#structured-results) is used to return the result of the worker task to anyone who wishes to use it.

Note that in a real world scenario, the task identifier should be implemented directly by the developer instead of using the simplified identifier, and it should represent a given worker in a non-configuration dependent way.

The worker task factory is implemented as follows:

[!embedcode](example_workertask/src/example/WorkerTaskFactory.java "language: java, range-marker-start: //snippet-start,  range-marker-end: //snippet-end, trim-line-whitespace: true")

For simplicity, we only return the input integer as the result of the task. In a real scenario, the worker task executes its designated work and returns a task result accordingly.

Additional benefits of the worker task approach is that is can integrate well with [computation tokens](taskcapabilities.md#computation-tokens), [cacheability](taskcaching.md) and [build clusters](buildclusters.md).

## Overwriting files

Tasks should not overwrite any of their input files. Doing so may incur race conditions when multiple tasks use the same input files and can result in unexpected incremental builds.

The tasks should take the contents of the input files and put their output files to a predefined location. Unless the user specifies otherwise, this should be the build directory.

## Build directory

The tasks should put their results in the build directory of the build execution. Implementations should note that the build directory is a shared output location with all the tasks in the build execution. Keeping this in mind, it is recommended that they specify their unique output location somewhere in the build directory.

As a convention, we recommend that the tasks put their results in the `builddir/task.name/identifier` directory. Where `task.name` is the preferably globally unique name of the task and the `identifier` is some user specified or configuration derived name for the task.

In the following example we create a file with the contents `outdata` and add it to the output directory that has the location of `builddir/example.task/default/output.txt`:

[!embedcode](example_builddirout/src/example/BuildDirOutputTaskFactory.java "language: java, range-marker-start: //snippet-start,  range-marker-end: //snippet-end, trim-line-whitespace: true")

An other identifier than `default` may be chosen based on user parameters or other configuration related information. It's recommended that task developers make sure that only a single task places their files in a given directory.

<small>

Also note that the above example should be modified when used with clusters according to the recommendations for [output files](buildclusters.md#output-files).

</small>



