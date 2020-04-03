# Build tasks

This document provides insights for developing build tasks for the saker.nest repository to be used with the saker.build system.

First of all, we recommend reading the [task development guide](root:/saker.build/doc/extending/taskdev/index.html) for saker.build.

In order for the repository runtime to find your build task classes, you need to declare them in the task file of the bundle. More information about it [here](bundleformat.md#task-file).

When creating a build task class, you must ensure that it can be instantiated by the repository runtime. The runtime will attempt to create a task instance using the following methods:

1. Calling a public static no-arg method with the name `provider` in the declared class. This is similar to the Java 9 [`ServiceLoader`](https://docs.oracle.com/javase/9/docs/api/java/util/ServiceLoader.html) provider method. The declared return type of the method must be assignable to [`TaskFactory`](root:/saker.build/javadoc/saker/build/task/TaskFactory.html), however, the enclosing class is not required to be assignable to it.
2. The runtime will use the public no-arg constructor to create a task instance. In this case the class must implement the [`TaskFactory`](root:/saker.build/javadoc/saker/build/task/TaskFactory.html) interface.

After the runtime instantiates the task, it will be passed to the build executor that will invoke the task. It is recommended that the actual [`Task`](root:/saker.build/javadoc/saker/build/task/Task.html) instance that is run implements [`ParameterizableTask`](root:/saker.build/javadoc/saker/build/task/ParameterizableTask.html). This ensures that any parameters that the user passes to the task will be passed to it.

The instantiated tasks that are used through the build scripts often don't have any attributes. We recommend extending the [`FrontendTaskFactory`](/javadoc/saker/nest/utils/FrontendTaskFactory.html) from the saker.nest repository client library that serves as a suitable base implementation.

The tasks that are used as a build script frontend shouldn't declare any capabilities, computation tokens, or environment selectors. We recommend that the only purpose of the frontend tasks are to parse the inputs passed by the user and start an appropriate worker task that runs based on the user parameters.
