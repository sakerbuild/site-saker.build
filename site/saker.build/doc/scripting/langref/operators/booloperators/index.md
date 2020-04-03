# Boolean operators

Operators that take two boolean operands are the following:

`||`: logical OR \
`&&`: logical AND 

The both evaluate their left and right operands and interpret them as booleans the same way as the [if-else statement](/doc/scripting/langref/ifelse/index.md) does.

Each operators work the same way as they do in other common programming language, aside that they don't support short-circuit evaluation. Both operands are evaluated, always.

The result of the operator expressions is based on the truthfulness of the operands.

**Logical OR truth table**

The following table specifies the result of the `||` operator given the operand values.

| Left 		|							| Right 	|		| Result	|
|---:		|:---:						|:---		|:---:	|:---		|
| `false`	|<code>&#124;&#124;</code>	| `false`	|`=`	| `false`	|
| `false`	|<code>&#124;&#124;</code>	| `true`	|`=`	| `true`	|
| `true`	|<code>&#124;&#124;</code>	| `false`	|`=`	| `true`	|
| `true`	|<code>&#124;&#124;</code>	| `true`	|`=`	| `true`	|

**Logical AND truth table**

The following table specifies the result of the `&&` operator given the operand values.

| Left 		|		| Right 	|		| Result	|
|---:		|:---:	|:---		|:---:	|---		|
| `false`	|`&&`	| `false`	|`=`	| `false`	|
| `false`	|`&&`	| `true`	|`=`	| `false`	|
| `true`	|`&&`	| `false`	|`=`	| `false`	|
| `true`	|`&&`	| `true`	|`=`	| `true`	|

```sakerscript
# simple usage
$left || $right
$left && $right

# using in an if condition
if $left || $right {
	# ...
}

# using in ternary
$left || $right ? $trueval : $falseval

# complex conditions
($first || $second) && ($third && ($fourth || !$fifth))
```

The AND (`&&`) operator takes precedence before the OR (`||`) operator. However, we recommend developers to use parentheses in order to improve readability of the code.

```sakerscript
# the following are the same given the precedence of the operators
 $first && second  || $third ||  $fourth && $fifth
($first && second) || $third || ($fourth && $fifth)

# the following is false
false && true || false
# same as 
(false && true) || false
```
