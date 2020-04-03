# Bitwise operators

Operators that take two numeric operands and operate on the bit representation of theirs are the following:

`|`: bitwise OR \
`&`: bitwise AND \
`^`: bitwise XOR

The operators take the binary representation of the numbers and result in an aggregated number that has its bits determined based on the operands and the operation type. When numbers with different precision are used in the expression the lower precision will be converted to the type of the greater precision number.

```sakerscript
1 | 2 # results in 3
1 & 3 # results in 1
1 ^ 3 # results in 2

0xF0 | 0x0C # results in 0xFC
0x0F & 0x0C # results in 0x0C
0x0F ^ 0x0C # results in 0x03
```

**Truth table**

The truth tables of the operators are the following for each bit in the operands:

| Left bit 	| Right bit 	|   	| OR 	| AND 	| XOR 	|
|:--------:	|:---------:	|:---:	|:---:	|:---:	|:---:	|
|     0    	|     0     	| = 	|  0 	|  0  	|  0  	|
|     0    	|     1     	| = 	|  1 	|  0  	|  1  	|
|     1    	|     0     	| = 	|  1 	|  0  	|  1  	|
|     1    	|     1     	| = 	|  1 	|  1  	|  0  	|

The above transformation will be applied for each bit present in both operands and the bits will be aggregated in the resulting number. The operator applies the transformation to the sign bit of the numbers as well.

when floating point numbers are operands of the operators, they may be converted to intergral numbers or an exception may be thrown in an implementation dependent manner.