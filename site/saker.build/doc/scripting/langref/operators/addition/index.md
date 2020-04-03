# Addition

Apart from [mathematical numeric additions](../mathoperators/index.md), the language supports merging lists and maps. The operator takes two arguments of the same type (list and list, or map and map) and results in a merged value of the same type.

```sakerscript
# concatenate lists
[1, 2] + [3, 4] # [1, 2, 3, 4]
[1, 1] + [1, 2] # [1, 1, 1, 2]
[1, 2] + 3 # error, right operand is not a list

# concatenate maps
{ K1: v1 } + { K2: v2} # { K1: v1, K2: v2}
{ Key: left, KX: x } + { Key: right, KY: y} # { Key: right, KX: x, KY: y}
```

When lists are being concatenated, the resulting list will have the elements of both lists and the elements of the first list will be ordered first in the result.

When maps are being merged, the resulting list will contain all of the key-value pairs from the left map and the right map. If there is a key collision, the key-value pair from the right operand will be present in the result. (I.e. the key-value from the left map is not present in the result.)

If the types of the operands cannot be interpreted to be the same and the operation cannot be completed, the implementation is required to throw an exception. The `+` operator supports adding numbers, which is described in [](../mathoperators/index.md). 

If any of the operands are `null`, the implementation may throw an exception.