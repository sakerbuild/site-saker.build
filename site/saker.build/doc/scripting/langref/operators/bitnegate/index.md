# Bitwise negate

The bitwise negate unary operator works on integers and flips every bit in the numeric representation. The operator expects the subject operand to be a number value. If not, a runtime exception is thrown.

The bits of the number will be negated, meaning that all bits in the current binary integral representation of the number will be flipped. If the subject number has arbitrary precision, then all bits up to the bit length of the representation will be flipped alongside with the sign of the number. If the number has a fixed precision (e.g. 64 bit), then all bits are flipped including the unused ones.

As bitwise operations are not applicable to floating point numbers, therefore implementations may convert the number to an integral representation in an implementation dependent manner or may thrown a runtime exception.

```sakerscript
# negate a literal: -2
~1
# negate hexadecimal: 0xFFFFFFFFFFFF8290 (64bit)
~0x7d6f
# negate a variable
~$var
```
