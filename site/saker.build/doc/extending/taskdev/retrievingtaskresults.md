# Retrieving task results

The build system allows task developers to retrieve the results of other tasks in various ways. This mechanism allows them to install the dependencies on other tasks, and use their results as part of their computations.

A task can retrieve results for other tasks given they know the associated task identifier for them. The [`TaskContext`](/javadoc/saker/build/task/TaskContext.html) provides the functions for getting the task results, and managing the dependencies accordingly.

Unless otherwise noted, all functions that retrieve the result of a task will wait for the given task execution to finish, and return their results accordingly. If they fail the execution, a `TaskExecutionFailedException` will be thrown.

When retrieving task results, make sure to handle the special cases of task results mentioned in [](taskresults.md).

## `getTaskResult()`

The simplest way of retrieving the result of a task is `TaskContext.getTaskResult`. The article [](taskparameters.md) provides the example for its usage:

[!embedcode](example_taskparameters/src/example/ExampleParameterizedTaskFactory.java "language: java, range-marker-start: createTask, marker-start-include: true, range-marker-end: //snippet-end, trim-line-whitespace: true")

Using it the following way:

```sakerscript
$paramvalue = "Hello world!"
example.task(Parameter: $paramvalue)
```

In the `initParameters` function we get the `Parameter` provided by the user, and retrieve the task result for the task that is associated with it. In the above example, the parameter task will get the value of the `$paramvalue` variable.

As the task result for the variable has been retrieved by our task, the build system will record a dependency for it. If we run the build without any modification, no tasks will rerun, as expected.

However, if we modify the value of `$paramvalue`:

```sakerscript
$paramvalue = "Modified world!"
example.task(Parameter: $paramvalue)
```

The build system will detect this, and discover that one of the input task dependency for `example.task` has been modified. This will cause it to be rerun, and print the `"Modified world!"` string accordingly.

## Task futures

Task futures are basically handles to a task result, and they can be retrieved them without having to specify the task identifier for the subject task. Using them can be more convenient, and they also provide a few extra functionality for fine-graining the recorded dependency.

The two interfaces `TaskFuture` and `TaskDependencyResult` provide this functionality. The two interfaces are seemingly equivalent, however, their usage are subject to different scopes.

### `get()`

In the following example we implement a task that invokes `substring` on an argument and uses futures to retrieve the results:

[!embedcode](example_taskfutures/src/example/SubstringTaskFactory.java "language: java, range-marker-start: //snippet-start,  range-marker-end: //snippet-end, trim-line-whitespace: true")

The task uses the built-in parameter assignment mechanism for the range of the substring, and initializes the unnamed input parameter directly to retrieve the task result later.

```sakerscript
example.substring("hello world", Start: 3, End: 8)
```

The above will result in the string `"lo wo"`. As the first statement of the task, we retrieve a `TaskFuture` for the input parameter task identifier. Calling `get()` on it will wait for the task to finish, and retrieve the result for in. In this case this will be `"hello world"`. We use the other parameters to calculate the string range to return, and call the `substring` method accordingly.

We could've used the previously mentioned `getTaskResult` method to retrieve the result, but we used a future to showcase its usage.

### Output change detection

In many cases you might only be interested partially in the result of a task. In these cases if other aspects of a task change than the ones you consume, you probably don't want your task to be rerun, as the produced result will be the same.

One example for this if we look at the previously implemented `example.substring` task. If we pass `"hello world"` or `"xyzlo woabc"` to it with the same range arguments, it will still produce the same `"lo wo"` result. Therefore, if the argument string changes only outside of the interested range, we don't want our `example.substring` task to be reinvoked, as that could cause unnecessary computations during build execution.

So in order to demonstrate this, we start with the following build script:

```sakerscript
$inputstring = "hello world"
example.substring($inputstring, Start: 3, End: 8)
```

Then we modify it to the following:

```sakerscript
$inputstring = "xyzlo woabc"
example.substring($inputstring, Start: 3, End: 8)
```

In both cases our `example.substring` task will produce the `"lo wo"` result, so we expect our task **not** to be reinvoked when the above changes occur in the build script.

We can achieve this by using the `TaskDependencyResult` interface in the modified implementation of our task:

[!embedcode](example_taskfutures/src/example/DependencySubstringTaskFactory.java "language: java, range-marker-start: //snippet-start,  range-marker-end: //snippet-end, trim-line-whitespace: true")

We use the `TaskDependencyResult` in the same way as `TaskFuture` to retrieve the result of the task. However, after determining the substring, we set the `TaskOutputChangeDetector` for the dependency on the input string task. The `TaskOutputChangeDetector` interface is used by the build system to determine if the task should be considered to be changed in relation to the caller, by providing an opportunity to compare the current output of the subject task.

The [`StringRangeTaskOutputChangeDetector`](example_taskfutures/src/example/StringRangeTaskOutputChangeDetector.java) class implementation checks if the given range of the task equals to the expected, and determines the change accordingly.

#### Incremental scenario

Let's examine what happens when we apply the build script changes as in the above example:

1. We run the build and then modify the script according to the previous section.
2. The build system will run the modified script and assign `"xyzlo woabc"` to `$inputstring`.
3. The build system will determine the chanes for `example.substring`.
	* It sees that the `Start` and `End` parameters haven't changed.
	* It sees that the value of `$inputstring` has been modified. Normally this would cause the rerun of `example.substring`.
	* It will invoke the `isChanged` method of the installed `StringRangeTaskOutputChangeDetector` for the dependency.
		* The change detector will determine that the interested range for the string is still `"lo wo"`
		* As the detector reports no changes, the build system will detect that the dependency on the value of `$inputstring` is *unchanged*.
	* The build system determines that `example.substring` doesn't need to be rerun, as no input tasks are considered as changed in relation to it.
4. The build finishes without invoking `example.substring`.
	
The above scenario is what we've wanted and using the task output change detector we've successfully achieved that. In a real world scenario, the tasks would be more compilicated than comparing part of a string, but the example showcases a minimal example for it. (One example for which we use this feature is when compiling multiple dependent Java projects. If the signatures of the Java classes doesn't change, then we don't need to recompile other projects that use them.)

Note that however, if we modify any other parameter of the task (e.g. `Start` or `End`), the task will be rerun accordingly.

### Future vs. dependency result

The interfaces `TaskFuture` and `TaskDependencyResult` seemingly responsible for the same things. However, their usage scope differs in the sense that a `TaskFuture` instance can be shared by multiple parts of the code, while a `TaskDependencyResult` should be constrained to a single use.

Task futures are stateless objects, and they provide access to the associated subject task for the caller. Task dependency results on the other hand are used to fine-grain a *single* dependency on the associated subject task, and are not to be shared with different parts of the code.

The `setTaskOutputChangeDetector` can be only called once for each dependency result instance.

In general, if you don't want to fine-grain the recorded dependency for the task, use `TaskFuture`. If you want to reify your dependency management, use `TaskDependencyResult`, however, get a instance of it for each semantically separate `get()` or related calls.

You can retrieve dependency result handles by calling `TaskContext.getTaskDependencyResult` or `TaskFuture.asDependencyResult`.

## Retrieving finished results

The `getFinished()` function of the task future interfaces allow retrieving the results of already finished tasks. Its significance only matters when you're normally not allowed to wait for tasks. These scenarios can happen if you're running short tasks or tasks with computation tokens. (See [](taskcapabilities.md#computation-tokens))

In order to be allowed to retrieve a finished result for a given task, the task must've been already waited for by the caller or any of the ancestor tasks of the caller. The ancestor tasks are the tasks which have directly or transitively started the caller task.

This requirement is actively enforced by the build system in order to prevent race conditions between tasks.

For simple task implementations, retrieving finished results usually play no role in the implementation. However, when designing tasks which are short, or request computation tokens, this functionality may be useful.

## Result waiting rules

In order to ensure deterministic behaviour and side-effect free task execution, the build system places some rules on how tasks can retrieve results of each other. The following must be true for all task which decides to wait for another:

> If given task *T* decides to retrieve the result of task *A*, then all of the ancestors of *A* must be finished until the common parent *P* of *T* and *A*.

**Why is this rule necessary?**

In the model of the build system, the tasks are to be side-effect free pure functions which produce their results only based on their inputs. This model also implies, that the outputs of a task should be made available to other tasks in a single (atomic) step, and no partial output of theirs should be visible.

If a partial output of a task is visible to other tasks, then other tasks may decide to change their behaviour based on this information. The trouble begins when tasks decide to cross-reference each others partial outputs, and decide to alter their execution based on the partial information. This can lead to serious race conditions, deadlocks and non-deterministic behaviour. These should strictly not occur in a build system, and therefore we decided to disallow such practices.

If this rule didn't exist, then tasks which were started by other tasks could be retrieved before their parents finish their executions. This would mean that the partial output of the parent tasks are available to thers via their started tasks. As this violates the above requirements, we placed the rule that specifies that in order to wait for a task result, the parent must've already been finished.

If two tasks have the same parent, then they can wait for each other, as they are considered to be the outputs of the same task, therefore not violating the above requirement.

**What does this mean in practice?**

For task implementations that doesn't do complicated task management, nothing. The build system is designed to be straight forward with this requirement, and only uncommon edge-cases may trigger a violation.

When such scenario happens, if you wait for a task that needs its parent waited for, then before returning the result of the waited task to the caller, the ancestor(s) will also be waited for. No task dependency will be recorded for the additionally waited ancestor(s).
