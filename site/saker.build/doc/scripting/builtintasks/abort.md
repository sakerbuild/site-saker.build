# `abort()`

The `abort()` task can be used to fail the currently running build execution with an optional message.

## Parameters

The following parameters are used by the task:

| Parameter 	       	| Description   	|
|----------------------	|---------------	|
| unnamed	| *Optional* message to print to the output and include in the thrown exception. 	|

## Task result

The task has no result, never returns successfully.

## Example

```sakerscript
if $condition < 123 {
	abort("Condition failed.")
}
```