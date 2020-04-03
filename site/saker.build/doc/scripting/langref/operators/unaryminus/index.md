# Unary minus

The unary minus operator is used in a prefix manner to a given expression. It can be applied only to numbers, in which case it will negate the numeric value of the subject expression.

```sakerscript
# negate a simple literal: -123
-123
# negate an expression: -6
-(2 * 3)
# negate floating numbers: -1.23
-1.23
```

The operator may promote the numeric precision of the subject value. 

E.g. if it is a `long` that equals the minimum value that can be represented in two's complement format (-9223372036854775808), then the negated value would overflow the 64 bit representation. In this case the implementation may either promote the value to an arbitrary precision integer, or proceed with the negation using 64 bits in an implementation dependent manner. \
(The build system implementation promotes to arbitrary precision `BigInteger`.)

If the operator receives a non-number as its subject, it will throw a runtime error.