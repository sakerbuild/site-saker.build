# Hello world

Let's start with the simplest task possible that only writes a simple string to the output:

[!embedcode](example_helloworld/src/example/HelloWorldTaskFactory.java "language: java, range-marker-start: public, marker-start-include: true")

The actual printing to the output happens in the `run(TaskContext)` method. It calls the `println(String)` method on the task context of the task, which will result in the `"Hello world!"` string to be displayed in the output.

All the other methods are seemingly extraneous, but they all have a specific functionality in order to work with the saker.build system properly. Let's view them one by one.

## `createTask(ExecutionContext)`

The overriden `createTask()` method of [`TaskFactory`](/javadoc/saker/build/task/TaskFactory.html) is called when the build system starts to invoke the task. A `Task` instance is created, that is a stateful object, and it will have its `run(TaskContext)` method called by the build system.

The [`ExecutionContext`](/javadoc/saker/build/runtime/execution/ExecutionContext.html) is the context object for accessing features of the build system that is associated with the current build execution. The [`TaskContext`](/javadoc/saker/build/task/TaskContext.html) is the context object for accessing features of the build system that is strictly related to the task, but not the whole execution. It can be used to report dependencies, get the deltas from previous run, start new tasks, and others.

In this `createTask()` method we will return a new anonymous class for our task, only for simplification reasons. In normal scenarios it's strongly recommened to export the task to a `static` inner class, or a new top level class.

## `run(TaskContext)`

This method is defined by the `Task` interface, and it is called when the build system invokes the task to execute its operations. This is the method where the computations are being done.

```java
taskcontext.println("Hello world!");
```

In the above implementation we just print out a simple message which will be displayed in the standard output of the build. When we execute the build the next time for our task, it won't be run, as it has not changed, but the build system will display the printed message again.

The `run` method declares itself to be able to throw any kind of `Exception` if the operations of it fails.

## Externalizable

We defined the `HelloWorldTaskFactory` to implement `Externalizable`. This is in order to have a more fine grained control over the serialization of the task factory. Using `Externalizable` instead of `Serializable` is likely to be more performant as well. Another note is that the saker.build system implements its own [serialization](serialization.md) methods for persisting the build database, in which `Externalizable` is strongly preferred.

## Equality

In the above example, the task factory has no fields. It will always do the same thing, therefore any other task factories which have the same class as `HelloWorldTaskFactory` is considered to be equal. In order to implement this, we only check the class identity of the argument in `equals`, using the `ObjectUtils` utility class.

```java
ObjectUtils.isSameClass(this, obj);
```

The `hashCode` computes its result based on the current class' name instead of calling `hashCode()` directly on the class. This is in order to have a stable hash code between executions. This is not required, but can be beneficial in some cases. (See [](taskcaching.md).)

```java
getClass().getName().hashCode();
```

## Constructor

The class needs a public no-arg constructor for two reasons. First is for it to be instantiatable via reflection when it is loaded. Second is because the `Externalizable` interface requires it in order to properly deserialize an instance.

Some repository implementations may not require a public no-arg constructor, but if you decide not to include one in your [`TaskFactory`](/javadoc/saker/build/task/TaskFactory.html) implementation, make sure you test it beforehand for proper operation.

## Further examples

In further examples, where the other methods are semantically the same as in the above example, we're not going to include them in the article example codes, and will only focus on the `run(TaskContext)` function or the `Task` implementation in whole.
