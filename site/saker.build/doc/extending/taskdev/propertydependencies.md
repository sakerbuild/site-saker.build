# Property dependencies

The build system allows reporting dependencies for environment and execution level properties. Properties are used to determine various aspects of the associated environment.

Such properties can be:

* Current Java Runtime version.
* Underlying operating system type.
* Value of an execution user parameter.
* Other custom defined properties.

These can be useful when a build task depends on a given configuration of the environment. In case these change, the task will be rerun with an appropriate delta.

Both the environment property (`EnvironmentProperty`) and execution property (`ExecutionProperty`) interfaces define a function (`getCurrentValue`) that is responsible for calculating their value based on the appropriate context. The calculated values should adhere to the equality functions of `Object`.

The property interface implementations should adhere to the `hashCode` and `equals` contract of `Object` as well. Clients can implement their own versions of these properties at will.

[!embedcode](example_propertydep/src/example/PropertyDependentTaskFactory.java "language: java, range-marker-start: //snippet-start,  range-marker-end: //snippet-end, trim-line-whitespace: true")

In the above example we retrieve the values of the specified user parameter based properties, and right away report them as dependencies using the function given in the task utilities. The `TaskContext.reportEnvironmentDependency` or `TaskContext.reportExecutionDependency` functions could've been used as well to do that.

In order to retrieve a property value without reporting it, you can use the `ExecutionContext.getExecutionPropertyCurrentValue` and `SakerEnvironment.getEnvironmentPropertyCurrentValue` functions.

When you report a property dependency, it will be checked again when the next build is run. If it changes, the task will be reinvoked with an appropriate delta.