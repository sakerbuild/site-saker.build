# Boolean negate

The boolean negate unary operator negates the boolean value of its subject. The subject expression is evaluated, and its value is interpreted as a boolean according to the same ruls as the [if-else statement](/doc/scripting/langref/ifelse/index.md).

```sakerscript
# negate a simple literal: false
!true
# negate a variable: true if $condition is false boolean
!$condition
# negate complex expressions
!($cond1 || $cond2)
```
