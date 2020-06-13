# Variables

The language allows declaring variables which are named storages of an expression value. Variables are uniquely identified by their name, and share the same build target scope.

```sakerscript
# assignments
$var = 123
$multiplied = $var * 3
```

Variables are dereferenced by using the `'$'` character before the subject expression. The dereferenced expression is usually a string literal, but other expressions are also allowed to support more refined usage of variables:

```sakerscript
# assigning non-literal variable names
$get.variable.name() = some_value
$("var{ $varnum }") = 123
```

A variable with a given name can only be assigned once in a build target. Once it has been assigned, any other assignments to it will result in an exception.

The [dereference operator](/doc/scripting/langref/operators/dereference/index.md) is applied according to the [precedence rules](/doc/scripting/langref/operators/index.md#precedence-table) of the language.

Variables can also be accessed by using the [`var()`](/doc/scripting/builtintasks/var.md) built-in task.

## Scope of variables

Variables share the scope of the build target they're declared in. There are no local variables, and assigning, using, or modifying a variable in a seemingly enclosed scope will be not limited to that scope, but to the whole build target scope.

```sakerscript
# variable among the global expressions
$outervar = "__TOKEN__"# ...
build{
	# variable in the build target, 
	# named the same as used among the global expressions
	# they are different variables
	$outervar = "__TOKEN__"#...
	
	$targetvar = "__TOKEN__"#...
	
	if example.condition.task() {
		# both references the vars in the build target:
		$outervar
		$targetvar
		# assign a variable of the build target
		$ifvar = 1
	} else {
		# assign a variable of the build target
		$ifvar = 2
	}
	
	# use the variable assigned in the above if statement
	example.task($ifvar)
	
	# same named var as in the below loop
	$item = "__TOKEN__"# ...
	foreach $item in [1, 2] {
		# shadows the $item variable in the build target scope
		$item
		# access the variable $item in the build target scope
		$"item"
		
		# assign a variable of the build target
		# probably erroneous as a variable may be assigned only once
		$foreachvar = "__TOKEN__"# ...
	}
}
```

As variables can be declared among the global expressions, they cannot be accessed from any of the build target expressions. The global expressions are considered to be a separate build target scope of their own in regards to this.

If a variable is assigned in an [`if-else`](../ifelse/index.md) statement then it will be visible in the enclosing build target scope.

If a variable is assigned in a [`foreach`](../foreach/index.md) expression then it will be visible in the enclosing build target scope. Note that it is probably erroneous to assign a variable in a `foreach` expressions, as a variable may only be assigned once, and a loop body may run multiple times, therefore assigning the variable more than once.

If one needs to declare file-level variables, see the [`static()`](/doc/scripting/builtintasks/static.md) built-in task. For global variables see [`global()`](/doc/scripting/builtintasks/global.md). (It is recommended that developers employ global variables very judiciously.)

## Dereference control flow

When a variable is dereferenced, the receiver expression (that is the one retrieving the value of the variable) will be put on hold. It will wait, until the variable is assigned. If the variable is already assigned when its value is being retrieved, the receiver expression will continue to run. If the variable has not yet been assigned, the execution of the receiver expression will be paused until it is assigned.

Based on this logic, it is possible to deadlock the execution of a script. When the variables depend on each other in a circular way, the execution of the script is erroneous, and will result in an exception during the execution of it.
One example for this:

```sakerscript
# circular variable dependency
$var = $secondvar * 3
$secondvar = some.task(Input = $var)
```

We can see that the assignment of `$var` will require the value of `$secondvar`. However, the assignment of `$secondvar` requires the output of `some.task(Input = $var)`, which needs the value of `$var` to run. In this case the execution of the script will deadlock.  

## Build target parameters

The declared parameters of the build target affect the behaviour of variables. Any input variables of a build target cannot be assigned in the body of the target. All output parameters which doesn't have an assigned value in the parameter list must be assigned during the execution of the build target. See [](../sourcefile/index.md) for more information.

```sakerscript
build(
	in InputParam,
	out OutputResult,
){
	# runtime error:
	$InputParam = "__TOKEN__"#...
	# assign the output of the target
	$OutputResult = "__TOKEN__"#...
}
```
