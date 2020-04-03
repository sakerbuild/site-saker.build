# If-else statement

The language allows declaring conditional blocks which execute based on the result of a boolean expression evaluation. This is similar to the known if-else control statements in other languages.

```sakerscript
# if-else condition based on a variable
if $condition {
	handle.true()
} else {
	handle.false()
}

# if-only condition
if $condition {
	handle.true()
}
```

If-else statements are declared by starting the statement with an `if` phrase. Then a condition expression should be declared, and the expressions to execute inside curly braces (`{ }`). Optionally the statement can continue with an `else` branch that is executed when the condition evaluates to `false`.

The condition expression result is interpreted as follows:

* If it is `null`, then the condition is considered to be `false`.
* If the condition result is a boolean instance, then the value of it will be taken.
* If not, then the result will be converterd to string representation, and it will be parsed to a boolean by checking if it equals to `true` in a case-insensitive manner.

If the condition doesn't evaluate to `true`, and there is no `else` branch, no expressions will be evaluated after the condition.

It's important to note that the if-else statement is **not** an expression, but a control statement. This means that it cannot be used in places where a value is expected. (E.g. at the right side of an assignment.)

Developers should keep in mind that if they assign any variables inside condition branches, the variables will be accessible from outside of the branches. If you assign assign a variable, make sure to assign it in both branches, else the variable may stay unassigned and cause a deadlock.

```sakerscript
# if-else assigning a variable
if $condition {
	$var = handle.true()
} else {
	handle.false()
}

# if $condition is false, the following call may 
# cause a deadlock if $var is not assigned elsewhere
outer.task($var)
```
