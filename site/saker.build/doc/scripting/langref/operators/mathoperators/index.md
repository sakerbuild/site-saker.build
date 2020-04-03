# Mathematic operators

Mathematic operators accept two numeric operands and work on their numeric values:

`+`: addition \
`-`: subtraction \
`*`: multiplication \
`/`: division \
`%`: integer remainder

The operators take the numeric value of their number operands and apply the associated mathematical function to them. The resulting value will be the resul of the expression.

```sakerscript
4 + 6 # 10
1.2 + 3.4 # 3.6
5 + 6.1 # 11.1

2 - 10 # -8
1.2 - 3.4 # -2.2

7 * 8 # 56
6 * -4 # -24
2 * 1.75 # 3.5

12 / 5 # 2
12.0 / 5 # 2.4
20 / -1 # -20
0xff / 4 # 0x3f

20 % 6 # 2
3.4 % 1.4 # 0.6
3 % 1.4 # 0.2
3.1 % 2 # 1.1
-5 % 3 # -2
5 % -3 # 2
-5 % -3 # -2
```

The operators can employ precision extension, meaning that if the two operands have different precision, the lower one will be converted to the representation of the greater one. When the numeric value of the result would overflow for the applied precision, the implementations may either convert the result to a bigger precision representation, or handle the overflow in an implementation dependent way. 

The mathematic operations are executed the same way as the Java languge specification dictates. (See [JLS 15.18.2](https://docs.oracle.com/javase/specs/jls/se7/html/jls-15.html#jls-15.18.2) and [JLS 15.17](https://docs.oracle.com/javase/specs/jls/se7/html/jls-15.html#jls-15.17))

The above operators have different precedence based on the mathematical operations they execute. See the [precedence table](/doc/scripting/langref/operators/index.md#precedence-table) of the operators for more information.

If any of the operands are `null`, not a number, or 0 for the divisor or remainder operand, the implementations may throw an exception during runtime or handle it in an implementation dependent manner.

The addition operator can also be used with lists and maps. See [](../addition/index.md) for more information.