# Inner tasks

Inner tasks can be used to split up the work of a task into smaller units while leveraging the scheduling functionality of the build system. Inner tasks use the same [`TaskFactory`](/javadoc/saker/build/task/TaskFactory.html) interface to specify their execution, but they are not associated with a task identifier. They run in the same task context as their starter task.

Inner tasks are treated as simple functions that run in the same task context as the caller. Any operations that an inner task do will have an effect on the enclosing task. This includes reporting and retrieving dependencies, and other task related operations.

Additional important distinction of inner tasks is that they can run multiple times, and also be duplicated multiple times onto multiple build clusters at once.

Inner tasks can be started using the `TaskContext.startInnerTask()` method, which return a handle to the results of their executions.

Generally, inner tasks only add complexity to the task implementation, and developers should consider using plain threads instead. It is recommended to use them if reified build cluster or computation token management is the goal with some shared state between the dispatched operations. A good example for this may be is implementing a tester task that manages some shared state between the tests, but are possible to execute them on remote machines as well.

## Duplication

Inner tasks can be automatically duplicated, meaning that the build system can automatically invoke the inner task multiple times if necessary, and duplicate it onto multiple clusters when appropriate. This is all done in a single call to `startInnerTask` with an appropriately setup `InnerTaskExecutionParameters` argument.

This entails that the started inner task will be invoked multiple times in the context of the caller task. This is usually useful when operations need to be executed on a collection of elements.

## Inner task results

As inner tasks can be duplicated, their results are not straightforward to retrieve. There can be one or more results of the task and they are not immediately available after the `startInnerTask` call returns.

In order to accomodate this, the inner task results are available through an instance of the `InnerTaskResults` interface which is responsible for providing access to the results. The interface is basically a result set that retrieves the result of an inner task or waits for one to be available. It acts as an iterator over the results.

```java
InnerTaskResults<?> innerresults = taskcontext.startInnerTask(/* ... */);
for (InnerTaskResultHolder<?> res; (res = innerresults.getNext()) != null;) {
	Object res = res.getResult();
	// handle inner task result
}
```

The results are available through this interface instead of providing a `Collection` result, as returning a `Collection` would require the caller to wait all the inner tasks before the `startInnerTask` call returns, which is rarely beneficial for the caller.

Important to note that enclosing tasks are *not* required to wait and retrieve the results of their inner tasks. They are allowed to start inner tasks and basically *'abandoning'* them in a way, that they are still executing after the enclosing task finishes. It is a valid scenario, and the build system will automatically wait for any inner task to complete before marking the task as finished.

However, if the enclosing task doesn't wait for the inner task to finish, then if an inner task execution decides to throw an exception, then the enclosing task will be considered as failed.

## Further information

For more information see the [Javadoc for the inner tasks](/javadoc/saker/build/task/TaskContext.html#startInnerTask-saker.build.task.TaskFactory-saker.build.task.InnerTaskExecutionParameters-).
