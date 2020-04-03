# `print()`

The `print()` task can be used to simply print a message to the build output. The task can be mainly used for debugging purposes.

## Parameters

The following parameters are used by the task:

| Parameter 	       	| Description   	|
|----------------------	|---------------	|
| unnamed	| **Required** message to print to the output and include in the thrown exception. 	|

The message parameter will be converted to a string representation in an implementation dependent way.

## Task result

The task has no result. Attempting to use the result will result in a runtime exception.

## Example

```sakerscript
# print a string
print("Hello world!")
# print a list
print([1, 2, 3])
# print a variable
print($var)
```
