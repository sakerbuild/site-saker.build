# Bitwise shift operators

Operators that take two numeric operands and shift the bit representation of theirs are the following:

`<<`: left shift \
`>>`: right shift

These operators take two operands, where the left is the number that is the subject of the bit manipulation and the right specifies the number of places to shift the bits. 

```sakerscript
# shift left 1 by 4 places: 16
1 << 4
# shift left hexadecimal values: 0x380
0x1C << 5

# shift right positive numbers: 2
9 >> 2
# shift right hexadecimal: 0x15
0x57 >> 2
# shift right negative: -2
-5 >> 2
# right shifting -1 always results in -1
-1 >> 20

# shifting by negative. the two expressions are the same
$number << $shift
$number >> -$shift
```

The operators work on the two's complement binary representation of the subject number. It takes the sign of the number into account, and executes the shifting of bits accordingly. The left and right shift operators are interchangeable in a way, that a left shift operation is semantically the same as the right shift operation with the shifting number negated, and vice versa.

The operators are allowed to expand the precision of the shifted number in cases when the shifted bits on left would fall out of the binary representation. Implementations may throw an exception or provide an implementation dependent result if this behaviour is not supported.

When shifting right a negative number, the sign of the shifted number will be preserved, and sign extension will be done. This means that in case of fixed precision representation, right shifting a negative number will set the highest order bits to 1 to preserver the sign of the number.

Note that unlike the Java shifting operators, unsigned right shift is not supported (as the sign of the subject is always preserved), and all bits of the right operand is taken into account when shifting. (In Java, e.g. shifting a `long` will only take the 6 lowest-order bits for the number of places to shift.)

when floating point numbers are operands of the operators, they may be converted to intergral numbers or an exception may be thrown in an implementation dependent manner.