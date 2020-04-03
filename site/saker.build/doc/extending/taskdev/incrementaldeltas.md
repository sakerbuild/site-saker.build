# Incremental deltas

When subsequent builds are run, the build system will determine how the inputs of a given task have been changed. It produces a set of *deltas* which are the manifestation of the detected changes.

If the build system detects no changes, the associated build task will not run. If it does, the task will be invoked, and the calculated deltas will be made available for it in order to provide the task an opportunity for optimizing its execution.

The kinds of deltas are determined based on their type. The `DeltaType` enumeration lists the possible types of the deltas.

All task invocations will have at least one delta. If the task is being run for the first time, the delta with a type of `NEW_TASK` will be present. In any other cases, the appropriate deltas with the associated types is present.

The build tasks can retrieve deltas using two functions of the [`TaskContext`](/javadoc/saker/build/task/TaskContext.html).

**Note** that the non-file related deltas that are given to the task may be not a complete set of deltas. It is possible for the build system to skip calculating a non-file related delta if it already detects one that renders the computation of it useless. E.g. If the task factory for the task changes, the build system may decide to skip computing the deltas for property dependencies, or others.

## `getNonFileDeltas()`

The `TaskContext.getNonFileDeltas()` function returns all deltas which are not file-related deltas of the current task execution. These are related to general task configuration changes, or reported [property dependencies](propertydependencies.md).

The retrieved set of deltas may be empty. In this case, there are at least one file-related delta for the build task, and it is recommended to examine the deltas from `getFileDeltas()`.

## `getFileDeltas()`

The `getFileDeltas()` function provides access to the file related deltas. File related deltas implement the `FileChangeDelta` interface which provide access to the delta related [`SakerFile`](/javadoc/saker/build/file/SakerFile.html) instance if present. They either represent a change in the reported [input or output dependencies](filedependencies.md), or an input [file addition](fileadditiondependency.md).

The file deltas can also be accessed using the [tag](filedependencies.md#dependency-tags) that was used to report the actual dependency. This can help the semantic separation of different file purposes.

## Incremental workflow

A very important aspect of the build system is the way the deltas are handled and their way off accessing. The deltas are determined in two phases by the build system.

1. In the first phase, the build system checks if there are any task related changes. It will determine if any task execution related changes have been occurred since the last execution. This includes determining any configuration related changes, property dependencies, task dependency changes, and others. If so, then the build system will run the task without examining any further changes.
2. If there are no task execution related changes, then the build system will proceed to check the changes in the reported file dependencies. If there are file changes, it will run the task, but if there are none, the build system consider the task up to date, and skip running it.

As you can see, your task might be invoked without the file deltas being determined. This is due to the fact that in order to determine the file deltas, all input dependency tasks must be already waited for. If the build system determines that one of the input tasks have been changed, then it cannot calculate the file deltas, as the task implementation might do completely different things based on the changed result of the input task.

In that case calculating the file deltas may provide an invalid representation of the file system state, therefore providing incorrect incremental information to the task.

In order to accomodate the above, the file deltas will be automatically calculated for the task when they are first accessed.

The above behaviour usually isn't an inconvenience, as the [recommended workflow](taskworkflow.md) for the task implementations are aligned with this. (In short: first wait for all input tasks, then access the files related to the build task.)
