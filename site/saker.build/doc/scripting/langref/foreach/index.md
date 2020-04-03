# Foreach expression

The `foreach` expression can be used to iterate over lists and maps, execute other expressions based on their elements, and generate new data based on them. Important to note that the `foreach` expression is not considered to be a loop, but a substitute of recurring expressions.

The simplest example of a `foreach` expression:

[!embedcode](simple.build "language: sakerscript, range-marker-start: #loop-start, range-marker-end: #loop-end")

In the above example we iterate over the list `[1, 2, 3]` with a loop variable of `$item`. For every item in the list we execute the `example.task()` with the item as parameter. The above is semantically the same as the following:

[!embedcode](simple.build "language: sakerscript, range-marker-start: #unroll-start, range-marker-end: #unroll-end")

The iterable object must not be a literal expression, other expressions can be used as well:

[!embedcode](simplemap.build "language: sakerscript, range-marker-start: #loop-start, range-marker-end: #loop-end")

The `foreach` expression can be used to iterate over maps as well. In that case two loop variables need to be declared separated by a comma. The first one will be the key of the current map entry and the second will be the associated value. The entries of a map may be iterated over without any specific order.

The above example is semantically equivalent to the following:

[!embedcode](simplemap.build "language: sakerscript, range-marker-start: #unroll-start, range-marker-end: #unroll-end")

## Local variables

The `foreach` expression allows declaring local variables which are private to the scope of the `foreach` expression:

[!embedcode](locals.build "language: sakerscript, range-marker-start: #loop-start, range-marker-end: #loop-end") 

The local variables can be declared in a comma separated list after the iterable expression, and optionally specified with an initializer that assigns the value of the variable. If an initializer is not present, the variable may be initialized at most once in the body of the loop.

If the `with` keyword is included in the expression, at least one local variable declaration must follow. The declarations allow and ignore extraneous commas.

The local variables can be accessed in the same way as the loop variables.

The above example is equivalent to the following when unrolled:

[!embedcode](locals.build "language: sakerscript, range-marker-start: #unroll-start, range-marker-end: #unroll-end")

Some other notable examples are the following:

[!embedcode](locals.build "language: sakerscript, range-marker-start: #other-examples-start, range-marker-end: #other-examples-end")

## Expression result

`foreach` expressions can be specified to produce a result. This can be done by appending the declaration by a result statement as follows:

[!embedcode](results.build "language: sakerscript, range-marker-start: #loop-start, range-marker-end: #loop-end")

The result declaration is specified by a separating colon (`':'`) character and the result expression. In the above, the result is declared to be a list with the elements muliplied by `3`. If the body of the loop is empty, it will be equivalent to the same:

[!embedcode](results.build "language: sakerscript, range-marker-start: #unroll-start, range-marker-end: #unroll-end")

The body of the `foreach` may be omitted if it contains no expressions. The following declaration is the same:

[!embedcode](results.build "language: sakerscript, range-marker-start: #emptybody-start, range-marker-end: #emptybody-end")

The result expression may be only one of the following expression types:

* `[]`: list, in which case the results will be aggregated in a resulting list
	* This is semantically same as if the [addition](../operators/addition/index.md) operator have been used for the declared result lists.
* `{}`: map, in which case the results will be aggregated in a map
	* The key-value entries declared in the result will be added to a common result map, and that will be the expression value. If there are recurring keys, the implementation is required to throw an exception.
* `""`: compound literal, in which case the results will be concatenated
	* The resulting strings of each iteration will be concatenated after each other. 

Some notable examples of using result expressions:

[!embedcode](resultexamples.build "language: sakerscript")

Important to note that while both the body and the result expression may be omitted, not both can be at the same time. `foreach` expressions without a body and a result declaration is erroneous.

## Nesting loops

`foreach` loops may be nested in each other. When nested loops are declared, the names of loop and local variables may not collide, and they may not shadow each other. Any such declaration is erroneous.

[!embedcode](nesteds.build "language: sakerscript, range-marker-start: #loop-start, range-marker-end: #loop-end")

All loop and local variables declared in an enclosing loop are visible for inner `foreach` expressions. The above example is semantically the same as the following:

[!embedcode](nesteds.build "language: sakerscript, range-marker-start: #unroll-start, range-marker-end: #unroll-end")

## Accessing `foreach` variables

Both the loop and local variables can be accessed by using the `$` dereference operator. It has similarities with the variables, with imposes a strict requirement for the access declarations.

The `foreach` variables cannot be accessed via dereferencing a complex expression. If you have a `foreach` variable named `var`, then the accessing syntax **must** be `$var`. Any other format will access the variable in the enclosing build target scope. Some examples for this:

[!embedcode](varaccess.build "language: sakerscript")