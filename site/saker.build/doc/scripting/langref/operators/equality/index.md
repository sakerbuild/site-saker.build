# Equality comparison

The following equality operators take two operands and compare them for equality:

`==`: Equal to \
`!=`: Not equal to

The operator evaluates its two operands and check their equality. The equality checking is as follows:

* If any of the operand is a number, the equality will be `true` if the other operand is a number too, and when promoted to the greater precision of the operands, their numeric value equal.
* If any of the operand is a list, the equality will be `true` if the other operand can be interpreted as a list, and their elements at the respective indexes equal to each other.
* If any of the operand is a map, the equality will be `true` if the other operand can be interpreted as a map, and they contain the same key-value entries, given that the values compare to be equal.
* If any of the operand is from an external source, the equality is determined in an implementation dependent way.
* If any of the argument is `null`, the equality is `true` if the other operand is `null` as well.

```sakerscript
1 == 0x1 # true
1 != 0x1 # false
literal == literal # true
literal6 == "literal{ 2 * 3}" # true
[1] == [1] # true
{ Key: v } == { Key: v } # true
2 == (1 << 1) # true

1 == [1] # false
{ } == { K: v } # false

true != false # true
false == string # false

$object == example.task() # implementation dependent

null == null # true
null == string # false

# for any generic expression, the following is always true
(!($a == $b)) == ($a != $b)
($a == $b) == ($b == $a) # commutative
```

The `!=` operator is equivalent to the `==` operator with the resulting boolean negated. The equality operators are commutative, meaning that swapping the operands yields the same result.

Developer are recommended not to compare complex objects to each other, but only check the equality of simple objects like strings, and numbers.