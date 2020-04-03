# `sequence()`

The `sequence()` task can be used to invoke the specified argument tasks in their order. Its usage is **strongly discouraged** and is only present to circumvent limitations of the associated task implementations.

The `sequence()` task doesn't guarantee that the argument expressions are going to be actually evaluated in order, and developers should use this with great care.

The task takes a single list argument as its unnamed parameter which specifies the tasks which should be invoked in order. A specified expression in the argument list will only be evaluated if all preceeding tasks have their result fully retrieved.

Note that if any of the argument list expressions is used outside of the `sequence()` call, the ordering guarantees may not be satisfied.

## Parameters

The following parameters are used by the task:

| Parameter 	       	| Description   	|
|----------------------	|---------------	|
| unnamed	| **Required** list of expressions to evaluate.	|

The unnamed parameter must be a list literal defined by the SakerScript language.

## Task result

A list that has each of its element correspond to the expression result of the argument at the same index.

## Example

```sakerscript
# execute the following tasks in order
sequence([
	example.compile.sources(),
	example.package.library(),
	example.publish.library(),
])
```

The above example will compile some sources, package the results into some format, and publish them to some location. It is strongly unrecommended to implement and use tasks this way, as this violates the build language task [pure functional](/doc/scripting/langref/tasks/index.md/#pure-functions) requirements. This makes the build fragile, and reduce its performance.

Instead of the above, tasks designers should strive to make them useable in the following structure:

```sakerscript
# compile sources
$compiled = example.compile.sources()

# package the compiled sources
$package = example.package.library($compiled)

# publish the created package
example.publish.library($package)
```

This way the build system and the language can properly parallelize the tasks, as their input and output dependencies define their ordering. The tasks should communicate using their inputs and outputs, and not via a shared resource only (e.g. file system). Not depending on the outputs of other tasks when necessary could lead to serious ordering issue and undeterministic builds. 