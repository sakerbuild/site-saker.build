# Task extensions

Tasks are the extension points of the scripting language. They are named, pure functions that produce their outputs based on the input information of the script and the execution environment. 

As SakerScript is designed to serve as a configuration language, it needs some mechanism that allows calling external code by the scripts. These are called tasks which can be provided by an implementation dependent extension mechanism for the language implementation. This is provided via the support of [repositories](/doc/guide/repositories.md) in the build system.

## Task names

The name of the tasks are lowercase name components separated by `'.'` characters. Tasks can have additional qualifiers that help choosing the appropriate implementation based on arbitrary properties.

```sakerscript
# simple task invocation
example.simple.task()

# task invocation with parameters
example.simple.task(FirstParam: 123, SecondParam: 456)

# task with qualifier
example.qualified.task-qual1-qual2()

# task with computed qualifier
example.qualified.task-{ "qual{ 3 * 2}" }()
```

Task invocations can be declared by specifying their name, and appending a parameter list after it. Parameters are defined in a parentheses enclosed list of key-value pairs.

Task names are dot separated components that match the regular expression of `[a-zA-Z_0-9]+`.

Qualifiers reside between the task name and parameter list and are prefixed by the hyphen (`'-'`) character. Tasks can have an arbitrary amount of qualifiers, and they need to match the regular expression of `[a-zA-Z0-9_.]+`. Note that qualifiers may contain the `'.'` character.

Based on the above, full task names must match the regular expression of `[a-zA-Z_0-9]+(\.[a-zA-Z_0-9]+)*(-[a-zA-Z0-9_.]+)*`. Note that although the mentioned regular expressions allow uppercase letters, they will be converted to their lowercase correspondent when looking up the task to execute.

Qualifiers of a task may be computed, meaning that arbitrary expressions may be specified to evaluate the qualifier value dynamically. Computed qualifiers must be enclised in curly braces (`{ }`). The result of the expression will be converted to string representation after evaluation. If the result doesn't match the required format, an exception is thrown.

### Name collisions

Task names are recommended to be specified in a way that is globally unique. However, collisions may occur. In order to avoid incompatibility due to this, the language allows to specify the name of the extension that should be used when looking up the task. This is basically the namespacing feature of the language for task names.

The extension identifier can be specified by appending the task name with the given identifier in an `@<id>` format.

```sakerscript
# simple task invocation from "ext1" extension
example.simple.task@ext1()
# simple task invocation from "ext2" extension
example.simple.task@ext2()
# qualified task invocation from "ext3" extension
example.qualified.task-qual1@ext3()
```

In the above example, `example.simple.task@ext1()` will be looked up from the extension that bears the name `ext1`. Similar behaviour applies to the others as well.

The extension identifiers must match the regular expression of `[a-zA-Z_][a-zA-Z0-9_]*`. Extension identifiers are case sensitive.

The build system supports this feature by mapping the extension identifiers to a repository identifier of the build configuration. (See [](/doc/guide/repositories.md))

## Parameters

Task parameters are specified as a parentheses enclosed list of key-value pairs. This is similar to maps (See [](/doc/scripting/langref/literals/index.md#maps)).

Parameter names are always explicit, and cannot be computed. Their names must be unique in the context of the task invocation, and multiple definitions of the same parameter will result in an error.

```sakerscript
# task invocation with parameters
example.simple.task(FirstParam: 123, SecondParam: 456)

# task invocation with unnamed parameter
example.unnamed.param.task(value)
# task with unnamed parameter and more parameters
example.unnamed.param.task(value, OtherParam: 789)
```

All the parameter names must be specified, but tasks can specify a single unnamed parameter for which the naming can be omitted. This must be the first parameter in the parameter list, and the language will pass it to the task as the parameter with empty name. Task implementations are not required to support this feature, but are encouraged to do so.

## Task results

As tasks are considered to work like functions, they can return a result value from their execution. It is a single arbitrary value that can be handled from the scripting language side.

```sakerscript
# simple task invocation
$simpleres = example.simple.task()
# task invocation using the simple result
example.consumer.task(Input: $simpleres)
```

The results of tasks are not handled specially by the language. They can be used the same way as other literals or structures in the language.

In the above example, the result value of `example.simple.task()` is assigned to the variable `$simpleres`, and then the task `example.consumer.task()` is invoked with the mentioned result as its `Input` parameter. (See [](/doc/scripting/executionmodel.md))

The tasks may not provide a result of their executions, in which case it is considered to have the `null` value. (See [](/doc/scripting/langref/literals/index.md#null))

## Pure functions

The tasks are pure functions that should adhere to the following requirements:

* Their outputs is a function of their inputs which include the input parameters and the state of the execution environment. They should not read any shared resource that cannot be accessed through the inputs. (The environment state is considered to be immutable.)
* They should not produce visible side-effects to other unassociated tasks. Meaning that they cannot modify any shared resource that is accessible from other tasks and would cause their result to be different without requiring the result of the task as their input.
* Given the same inputs, their results should be identical.

The above requirements allow the language execution model to parallelize the expression evaluations and allow the build system to properly employ optimizations for incremental build features.

Note that while the above definition works well on a task level, developers may need to employ further control-flow reifications in order to properly order task executions. (See also: [](/doc/guide/bestpractices.md))

In a more formal definition, given a task named `a()` is considered to be pure function if for any possible task implementation `b()`, invoking `a()` first and then `b()`, would produce identically same overall results as if invoking `b()` and then `a()`.
