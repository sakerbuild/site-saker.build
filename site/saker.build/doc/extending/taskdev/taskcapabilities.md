# Task capabilities

The [`TaskFactory`](/javadoc/saker/build/task/TaskFactory.html) interface provides various methods for subclasses to implement in order to customize the aspects of task invocation.

## Execution environment selection

The task factories can choose which build environment is suitable for their execution. This can be changed by implementing the `getExecutionEnvironmentSelector()` method, and providing a suitable implementation of the `TaskExecutionEnvironmentSelector` interface. The reponsibility of the returned object is to determine if the argument in its `isSuitableExecutionEnvironment(SakerEnvironment)` method implementation is capable of properly executing the associated task.

This functionality is mostly used in conjunction with remote execution in order to determine if the given environment has all the necessary tools and properties available for invoking the task. 

[!embedcode](example_envselector/src/example/ExampleEnvironmentSelector.java "language: java, range-marker-start: //snippet-start,  range-marker-end: //snippet-end, trim-line-whitespace: true")

In the above example, we ensure that the given environment has the user parameter of `"example.required.tool.version"` defined, which is a version number of a hypothetical tool that is used by our task. If it is missing, we deem the environment to be unsuitable for the task invocation by returning `null`.

If the tool version user parameter is defined, we return an appropriate results to signal that we can use the given environment. The result of the method needs to contain all of the environment properties which were used to check if the environment is useable. These properties will be tracked by the build system and incremental builds will be handled accordingly.

If the selector deems no environments to be suitable, the execution of the task and therefore the build will fail.

## Capabilities

The task can report *capabilities* that it supports. Capabilities are defined as well-known strings which are interpreted by the build system in order to execute the task accordingly. The values of the capability strings are declared among the `TaskFactory.CAPABILITY_*` constants.

The capabilities can be specified by overriding the `TaskFactory.getCapabilities()` function.

### Short tasks

Tasks can be declared to be short in regards to their execution time. Tasks can be considered to be short if their execution time is comparable to the time that it takes to start a new thread. If a task does no heavy computation, doesn't attempt to wait for other tasks and don't do I/O operations, they are good candidates to be short.

When short tasks are started, the build system will not start them on a separate thread, but instead run them directly on caller thread that started the task. This can be beneficial as it doesn't need any thread creation and much less synchronization with concurrent tasks.\
However, in order to achieve this, short tasks have a few restrictions:

* They can only wait for tasks which are also short.
* They cannot wait for tasks which have not yet started.
* They cannot be [remote dispatchable](#remote-dispatchable).
* They cannot use [computation tokens](#computation-tokens).

When a task starts a short task, then it will finish before the called `startTask` call returns.

### Remote dispatchable

If a task specifies this capability, then the build system will consider dispatching it to remote build cache. See [](buildclusters.md).

### Cacheable task

Using this capability the task can signal to the build system that it may be retrieved from cache and may be published to a build cache if possible. See [](taskcaching.md).

### Computational inner tasks

Signals to the build system that started inner tasks from this task may use computation tokens.

This capability is required for placing the proper restrictions on the enclosing task. This is necessary as the inner tasks run in the same task context as their parent, therefore the build system needs to place the restrictions of the computation token use to the parent as well on the inner tasks. If the build system didn't do that then it would be possible to deadlock the build execution by exhausting the computation tokens. (See [](#computation-tokens))

## Computation tokens

A computation token represents one unit of computational operation that uses one CPU thread on 100% usage. It is used to allocate the necessary CPU resources for tasks in order to properly schedule their execution in a way that reduces the thrashing effect of too many concurrently running operations.

When too many tasks attempt to use the CPU, a significant number of threads may be created in the process. If the number of active threads exceeds the number of threads the CPU can concurrently run, then the operating system needs to often switch the threads it executes. When these context switches occur too frequently, the CPU will end up spending too much time doing these instead of actually executing meaningful build related work.

In order to avoid/reduce this effect, tasks may request computation tokens to use, which roughly specify how many threads will the given task use when doing its operations. The build system will schedule these tasks in a way that they only start to run when enough computation tokens are available for them to take.

Computation tokens are shared resource in the JVM, and all currently running builds and tasks share the token pool. 

Tasks which request computation tokens may not wait on other tasks. This is in order to prevent deadlocking the build as if a task which request computation tokens attempts to wait on another task that requests computation tokens can easily deadlock the build if the waited task cannot start because it cannot allocate the tokens.

If they want to retrieve the result of a task, they can retrieve them as [finished results](retrievingtaskresults.md#retrieving-finished-results).

Computation tokens can be reported by overriding the `getRequestedComputationTokenCount()` function.

By default, the computation tokens are specified to have 1.5x more tokens than the number of [available processors](https://docs.oracle.com/javase/7/docs/api/java/lang/Runtime.html#availableProcessors()) of the JVM. This can be overridden by specifying the `saker.computation.token.count` system property. (`-D` command line flag) The computation token count will always be at least 2.
