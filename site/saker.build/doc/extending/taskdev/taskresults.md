# Task results

Each task invocation can return one result that will be considered as *the* result of the task. It will be accessible to other tasks and from build scripts. The below example will take two parameters, `Left` and `Right`, and returns the sum of theirs: 

[!embedcode](example_sum/src/example/SummarizeTaskFactory.java "language: java, range-marker-start: //snippet-start,  range-marker-end: //snippet-end, trim-line-whitespace: true")

Using them in the following way:

```sakerscript
$firstsum = example.sum(Left: 4, Right: 6)
print($firstsum)

$secondsum = example.sum(
	Left: $firstsum, 
	Right: example.sum(Left: 10, Right: 30),
)
print($secondsum)
```

Will print out `10` for the first sum of `4 + 6`, and `50` for the `$secondsum`, as it will add `10` to `10 + 30`.

When you run the build without any modifications, it will print out `10` and `50` again. However, if you decide to modify the first invocation of `example.sum`, like the following (`Right: 6` modified to `Right: 16`):

```sakerscript
$firstsum = example.sum(Left: 4, Right: 16)
print($firstsum)

$secondsum = example.sum(
	Left: $firstsum, 
	Right: example.sum(Left: 10, Right: 30),
)
print($secondsum)
```

Then with the modification, the first will print out `20`, and the second will print out `60`. You can notice that the modifications of the first task transitively caused all the dependent tasks to re-run, and print out the updated values.

This happens because when you retrieve a result of a task, the build system will automatically record a dependency on the associated task. Whenever that task is considered to be changed, any dependent task will re-run.

## Failing the task

Failing the task will mean that the build execution will abort and report an error. In order to do that, you are provided with the following ways:

* Throw an exception from `Task.run(TaskContext)` method.
	* Throwing an exception that is propagated to the caller build system will result in task failure.
* Call `TaskContext.abortExecution(Throwable)`.
	* The `abortExecution` method can be used to set the result of the task to be an exception.
	* Calling this method will still consider the task to be successfully finished, and the build system will consider the exception to be its result. It's important to note that if no dependencies of the task change, it won't be rerun in subsequent build executions, but will fail the build without invoking the task.
	* When you use this function, your task *must* return `null` from the `Task.run(TaskContext)` method.

If you fail the task execution, any other tasks which attempt to [retrieve the result](retrievingtaskresults.md) of this task will face a `TaskExecutionFailedException`.

## Structured results

The build system defines a special type that can be used as the return value of a task execution. They are an instance of the `StructuredTaskResult` interface which signals that the actual data behind the object are available via the results of other tasks.

The interface was defined in order to increase the performance of the build by improving the concurrency of the executed task. We can see it by looking at the following example:

```sakerscript
$list = [1, long.running.task(), 3, 4]
foreach $item in $list {
	# do something with the items
}
```

In the above example we execute some operations on each element of the list assigned to the `$list` variable. The `foreach` statement will get the value of the variable, and start the tasks for each element in the list. However, without the introduction of `StructuredTaskResult`, in order to retrieve the elements of the list, the `foreach` loop would need to wait for all of the elements in the list to evaluate.

This means that in order to execute the loop body for the elements `1, 3, 4`, the `long.running.task()` needs to finish, as only then can the value of the list be retrieved. This is not beneficiary as it seriously limits the possible concurrency of the build system via introducing an implicit synchronization point.

To get over this issue, the `StructuredTaskResult` interface was introduced. It is a common superinterface for other specializations such as `StructuredListTaskResult`, `StructuredMapTaskResult` and others. These interfaces provide access to the various enclosed data in the object by making them available through their associated task identifiers.

To apply the above to the given example, using `StructuredTaskResult`s will result in the `foreach` loop invoking its body for `1, 3, 4` right away, instead of waiting for `long.running.task()` to finish.

As a task developer, you can gradually implement support for `StructuredTaskResult`s and its different interface specializations. If you want to access the actual value instead of using the structured representation, you can call `StructuredTaskResult.toResult` method with the task context as the argument, or use the `StructuredTaskResult.getActualTaskResult` static methods to retrieve the task results.

### Example use

The following example shows how you can handle the structured nature of a task result:

```java
TaskIdentifier taskid; // the dependent task id
Object result = taskcontext.getTaskResult(taskid);
if (result instanceof StructuredTaskResult) {
	if (result instanceof StructuredListTaskResult) {
		// handle it as a list
		return;
	} 
	if (result instanceof StructuredMapTaskResult) {
		// handle it as a map
		return;
	} 
	result = ((StructuredTaskResult) result).toResult(taskcontext);
}
// handle the actual result value of the task without any 
//    structured nature 
```

Using the `instanceof` operator you can determine the kind of structure the given task result has. If you don't recognize the actual structure, calling `toResult` will get the actual representation of the object.

If you don't want to deal with structured results at all, you may do the following instead:

```java
TaskIdentifier taskid; // the dependent task id
Object result = StructuredTaskResult.getActualResult(taskid, taskcontext);
// handle the actual result value of the task without any 
//    structured nature
```

The `getActualResult` method will convert the possibly structured nature of the result to its actual representation. 

## Tagged results

When tasks are rerun for incremental changes, they often need to be able to store some internal state between executions. This can be beneficial when the task implementation needs to specifically determine the changes since the last execution and revalidate the internal state model. In order to support this, the build system allows tasks to store arbitrary output values for themselves.

The `TaskContext.setTaskOutput` function allows tasks to set arbitrary tag-value pairs to store between executions. These are not strictly *outputs* of the task, but rather an internal state storage. These values will not be visible for other tasks.

When a task is rerun, it can retrieve the values set in the previous run by calling `TaskContext.getPreviousTaskOutput` and passing the tag for the value.\
The value returned from `Task.run(TaskContext)` is also available using the `TaskContext.getPreviousTaskOutput` function.

Note that these functions may return `null` if the given values cannot be loaded or otherwise unavailable to the task. Task implementations should handle gracefully when these values are missing.
