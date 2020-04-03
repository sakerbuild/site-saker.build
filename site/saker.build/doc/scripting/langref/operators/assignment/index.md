# Assignment

Binary operator that assigns the right hand operand to the left hand operand. The left hand operand must be assignable. In general, retrieving the value of the left operand after the assignment is complete should result in the value of the right hand operand.

This operator is mainly used to set the values of variables. Although this is the only use-case right now, other assignable expressions may be introduced in the future or by the language implementation.

The operator is also used to set [`static`](/doc/scripting/builtintasks/static.md) and [`global`](/doc/scripting/builtintasks/global.md) variables. (This feature is implementation dependent.)

```sakerscript
# assign 123 to the variable named "var"
$var = 123
# assign 456 to the variable named "var6"
$"var{ 2 * 3 }" = 456
```
