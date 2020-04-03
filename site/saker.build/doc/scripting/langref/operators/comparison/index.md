# Numeric comparison

Comparison operators accept two numeric operands and determine their relation based on their numeric values:

`>`: Greater than \
`>=`: Greater than or equal to\
`<`: Less than \
`<=`: Less than or equal to

The operators take the numeric value of their number operands and compare them by value using the associated semantics to the given operator. The result of the comparison is a boolean value.

```sakerscript
# the below examples work the same when
# floating point numbers are used
4 < 6 # true
6 < 4 # false
4 < 4 # false

4 <= 6 # true
6 <= 4 # false
4 <= 4 # true

4 > 6 # false
6 > 4 # true
4 > 4 # false

4 >= 6 # false
6 >= 4 # true
4 >= 4 # true
```

The operators can employ precision extension, meaning that if the two operands have different precision, the lower one will be converted to the representation of the greater one.

The results of the comparison operators are implementation dependent when any of the operand is a `NaN` floating point value.

If any of the operands are `null`, the implementations may throw an exception during runtime or handle it in an implementation dependent manner.
