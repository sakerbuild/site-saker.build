# Expressions

Expressions are the basic unit of execution in the language. It can be simple (e.g. literals such as `"abcd123"`) or complex (e.g. by using operators like `123 + 456`). All of them have a result which can be used with other expressions.

The expressions can be declared in body of an execution block. Execution blocks are part of build targets, if-else statements, foreach loops and the global expression scope. Declaring an expression will result in it being evaluated when the enclosing expression or execution block is being evaluated.

Root expressions are the ones that have no enclosing expression, but directly declared in an execution block.

```sakerscript
build {
	# root expression
	example.task()
	# calc.task() is not a root expression
	#    but the whole "$sum = 123 + calc.task()" is
	$sum = 123 + calc.task()
}
```

## Declaring expression statements

When declaring root expressions, they need to be followed by expression statement closing character(s). This may be either a semicolon (`';'`) or newline characters. This is in order to avoid the language parser to involuntarily merge expression statements together.

```sakerscript
# followed by semicolon
example.task();
# semicolons allow multiple expressions on the same line
example.task(1);example.task(2); 

# followed by new line
example.task()

# followed by comment
example.task()# ending comment
example.task() # ending comment with spaces between
# multiline commends can be used as well
example.task() ### multiline comment ###
example.task() ### comment on
	multiple
lines ### example.task()
```

## Using operators

Using operators in the language also have requirements for their syntax. When using an operator with an operand that preceeds its, the operator must be declared on the same line as the preceeding operand.

```sakerscript
# single line complex expression
$var = 123 + 456

# complex expression spread over multiple lines
$var = 123 +
	456
```

When expressions are spread over multiple lines, the operator must be on the preceeding line than the next operand. The following declaration will not work as expected:

```
$var = 123
	+ 456
```

As we allow the new line to be an expression closing character, the assignment to `$var` will have the value of `123` instead of `123 + 456`. This is because the assignment statement is considered to be complete as a new line separator was encountered. The following expression is `+ 456`, which is invalid, as its left operand is missing, but the language parser cannot properly look ahead.

If the `+` operator is declared after the literal `123`, then the parser will expect another expression after it, so it will not consider the new line to be a valid expression closer.

It is also strongly recommended to have the operator tokens surrounded by whitespace. [Simple string literals](../literals/index.md#simple-string-literals) will not stop when they encounter most of the operators, and the operator therefore will be part of the literal.

```sakerscript
# sum has the value of "123+456" as string 
$sum = 123+456
# sum has the value of 579 as number
$sum = 123 + 456
```
