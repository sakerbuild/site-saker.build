[!section](helloworld.md)
[!section](taskparameters.md)
[!section](taskresults.md)
[!section](filehandling.md)
[!section](propertydependencies.md)
[!section](taskmanagement.md)
[!section](incrementaldeltas.md)
[!section](taskcapabilities.md)
[!section](parameterparsing.md)
[!section](innertasks.md)
[!section](taskworkflow.md)
[!section](taskcaching.md)
[!section](buildclusters.md)
[!section](standardio.md)
[!section](serialization.md)
[!section](ideconfig.md)
[!section](reproducible.md)
[!section](bestpractices.md)

# Task development

Tasks are the basic units of execution in the build system. We recommend you read the following material beforehand:

* [](/doc/guide/runtime.md)
* [](/doc/guide/controlflow.md)
* [](/doc/guide/incremental.md)
* [](/doc/guide/repositories.md)
* [](/doc/scripting/langref/tasks/index.md)

## Basics

The first step of building your own task is to find a repository that can provide access to it. As the Nest repository is the default repository of tasks for the build system, we recommend using that. The following examples are going to use the Nest repository, but if you don't want to use that, you can freely adapt the examples on your own discretion. See [](/doc/extending/repositorydev/index.md) for developing repositories. We recommend using the [saker.nest](root:/saker.nest/index.html) repository to develop and publish tasks for the build system.

Each task in the build system implements the [`Task`](/javadoc/saker/build/task/Task.html) interface. They contain a single function [`run(TaskContext)`](/javadoc/saker/build/task/Task.html#run-saker.build.task.TaskContext-) that allows them to execute their operations for the given build execution. These task objects are stateful objects, every time a task is invoked, a new [`Task`](/javadoc/saker/build/task/Task.html) instance is created by the build system. The [`Task`](/javadoc/saker/build/task/Task.html) objects are created by their defining [`TaskFactory`](/javadoc/saker/build/task/TaskFactory.html) implementations. From now on, we're going to referer to the [`TaskFactory`](/javadoc/saker/build/task/TaskFactory.html) - [`Task`](/javadoc/saker/build/task/Task.html) pairs as *task* in general. 

## Task factories

Task factories are stateless objects that specify how a task should be executed. Their responsibility to exactly specify how a task is invoked and create their [`Task`](/javadoc/saker/build/task/Task.html) instances when necessary. A [`TaskFactory`](/javadoc/saker/build/task/TaskFactory.html) is an immutable object, which can be compared by equality to other [`TaskFactory`](/javadoc/saker/build/task/TaskFactory.html) objects. \
When two [`TaskFactory`](/javadoc/saker/build/task/TaskFactory.html) objects equal, it means that given the same build environment, they will use exactly the same inputs and produce semantically the same outputs. This equality is an important aspect of task factories, as the build system can detect changes based on these equality checks.

Task factories also specify different aspects of their task executions, and that is discussed later. (See [](taskcapabilities.md).)

Task factories are also strongly recommended to implement some serialization mechanism, preferrably implementing the [`Externalizable`](https://docs.oracle.com/javase/8/docs/api/java/io/Externalizable.html) interface.

## Task identifiers

Task identifiers are immutable objects that uniquely identify a given task in the build system. They are a subclass of [`TaskIdentifier`](/javadoc/saker/build/task/identifier/TaskIdentifier.html), and are required to implement the `equals(Object)` and `hashCode()` contract of the Java `Object` functions.

During an execution of a task, it can choose to start new tasks. Each task is identified by the supplied task identifier along with the task factory. During a single build execution only a single task can be run for a single task identifier.

Task identifiers are also used to look up the previous execution results to a task when it is run again. When a task with task identifier of `taskid` is run, and finishes successfully, then the next time it is run, the previous outputs of the task `taskid` will be provided to it in order to properly implement incremental functionality.

