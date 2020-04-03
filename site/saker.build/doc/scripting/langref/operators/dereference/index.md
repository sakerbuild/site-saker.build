# Dereference

Dereferencing an expression allows the code to access a variable with the specified name. The result of a dereference expression can be used as the left side operand of an assignment. (See [](../assignment/index.md))

Dereferencing a variable is semantically the same as using the [`var()`](/doc/scripting/builtintasks/var.md) task. Additionally, dereferencing differs from `var()` in a way that it can be used to access loop and local variables in the body of a [`foreach`](../../foreach/index.md) expression.

Both simple and complex expressions can be the subject of dereferencing.

See [](../../variables/index.md) for more information.

```sakerscript
# assign 123 to the variable named "var"
$var = 123
# assign 456 to the variable named "var6"
$"var{ 2 * 3 }" = 456
# assign the value of $var to $alias
$alias = $var
```
