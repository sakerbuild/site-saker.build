# `var()`

The `var()` task can be used to access a variable with the specified name in the enclosing build target scope. This task works the same way as accessing a variable via the [dereferencing operator](/doc/scripting/langref/operators/dereference/index.md). However it cannot be used to access expression specific local variables (e.g. [`foreach` loop variables](/doc/scripting/langref/foreach/index.md)). In any other cases, it works the same way as dereferencing an expression.

The result of the `var()` task can be used on the left hand side of an [assignment expression](/doc/scripting/langref/operators/assignment/index.md).

## Parameters

The following parameters are used by the task:

| Parameter 	       	| Description   	|
|----------------------	|---------------	|
| unnamed	| **Required** name of the variable that is being accessed.	|

## Task result

The reference to the variable.

## Example

```sakerscript
# the following is the same
$num = 3
var(num) = 3

foreach $item in [1, 2] {
	# accesses the item variable, not the loop variable
	var(item)
	# accesses the loop variable
	$item
}

build(in Input) {
	# accesses the Input parameter
	var(Input)
}
```
