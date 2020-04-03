# Task parameters

Tasks don't just automatically accept parameters from external sources. That is, you need to take extra measures in order to make your tasks parameterizable from build scripts. In order to support this, the build system defines the `ParameterizableTask` extension interface.

[!embedcode](example_taskparameters/src/example/ExampleParameterizedTaskFactory.java "language: java, range-marker-start: createTask, marker-start-include: true, range-marker-end: //snippet-end, trim-line-whitespace: true")

In the above, instead of creating a task instance of `Task`, we create an instance of `ParameterizableTask`. The `ParameterizableTask` interface contains the interface method `initParameters` which is called by the build system before `run(TaskContext)`. It is an initialization method that allows the task object to retrieve and assign the parameters of the task.

Running the above example in the following way:

```sakerscript
example.task(Parameter: "Hello world!")
```

Will print out `"Hello world!"` accordingly.

The parameters are available through the `Map` argument which contains all the named parameters that were given to the task invocation. Each `String` key maps to the identifier of a task that provides the value of the given parameter. You can use the task context to retrieve the results for the given parameter task identifier, or handle the identifier in any other ways.

`ParameterizableTask` comes with a default implementation that assign the annotated field parameters of a task objects based on their annotations. See [](parameterparsing.md) for more information about it. Using the default mechanism, the above is roughly equivalent to the following:

[!embedcode](example_taskparameters/src/example/DefaultExampleParameterizedTaskFactory.java "language: java, range-marker-start: //snippet-start,  range-marker-end: //snippet-end, trim-line-whitespace: true")

In which case the `Parameter` will be automatically assigned and converted to the `String parameter` field when the default implementation of `initParameters` is called.
