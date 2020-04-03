[!section](addition/index.md)
[!section](assignment/index.md)
[!section](bitnegate/index.md)
[!section](bitoperators/index.md)
[!section](bitshiftoperators/index.md)
[!section](boolnegate/index.md)
[!section](booloperators/index.md)
[!section](dereference/index.md)
[!section](equality/index.md)
[!section](mathoperators/index.md)
[!section](comparison/index.md)
[!section](parentheses/index.md)
[!section](subscript/index.md)
[!section](ternary/index.md)
[!section](unaryminus/index.md)

# Operators

The articles in this section specify the usable operators in the language.

## Precedence table

The following table defines the order of evaluation when multiple operators present in an expression.

|                 	| Operator                  	| Reference                                                                    	|
|-----------------	|---------------------------	|-----------------------------------------------------------------------------	|
| Evaluated first 	| `(expr)`                  	| [](parentheses/index.md)                                                    	|
|                 	| `$expr`                   	| [](dereference/index.md)                                                    	|
|                 	| `[idx]`		             	| [](subscript/index.md)                                                      	|
|                 	| `~` `-` `!`	             	| [](bitnegate/index.md), [](unaryminus/index.md), [](boolnegate/index.md) 		|
|                 	| `*` `/` `%`               	| [](mathoperators/index.md)                                                  	|
|                 	| `+` `-`                 		| [](addition/index.md), [](mathoperators/index.md)                        		|
|                 	| `<<` `>>`                 	| [](bitshiftoperators/index.md)                                              	|
|                 	| `<` `<=` `>=` `>`         	| [](comparison/index.md)                                                     	|
|                 	| `==` `!=`                 	| [](equality/index.md)                                                       	|
|                 	| `&`                       	| [](bitoperators/index.md)                                                   	|
|                 	| `^`                       	| [](bitoperators/index.md)                                                   	|
|                 	| <code>&#124;</code>       	| [](bitoperators/index.md)                                                   	|
|                 	| `&&`                      	| [](booloperators/index.md)                                                  	|
|                 	| <code>&#124;&#124;</code> 	| [](booloperators/index.md)                                                  	|
|                 	| `cond ? tv : fv`            	| [](ternary/index.md)                                                        	|
| Evaluated last  	| `var = expr`              	| [](assignment/index.md)                                                     	|

## Syntax lookup

The following operators are available in the language based on their syntax:

* `(expression)` - [](parentheses/index.md)
* `$expression` - [](dereference/index.md)
* `left = value` - [](assignment/index.md)
* `subject[index]` - [](subscript/index.md)
* `list + list` - [](addition/index.md)
* `map + map` - [](addition/index.md)
* `number + number` - [](mathoperators/index.md)
* `number - number` - [](mathoperators/index.md)
* `number * number` - [](mathoperators/index.md)
* `number / number` - [](mathoperators/index.md)
* `number % number` - [](mathoperators/index.md)
* `boolean && boolean` - [](booloperators/index.md)
* `boolean || boolean` - [](booloperators/index.md)
* `!boolean` - [](boolnegate/index.md)
* `left == right` - [](equality/index.md)
* `left != right` - [](equality/index.md)
* `number > number` - [](comparison/index.md)
* `number >= number` - [](comparison/index.md)
* `number < number` - [](comparison/index.md)
* `number <= number` - [](comparison/index.md)
* `condition ? trueval : falseval` - [](ternary/index.md)
* `-number` - [](unaryminus/index.md)
* `~number` - [](bitnegate/index.md)
* `number | number` - [](bitoperators/index.md)
* `number ^ number` - [](bitoperators/index.md)
* `number & number` - [](bitoperators/index.md)
* `number << shift` - [](bitshiftoperators/index.md)
* `number >> shift` - [](bitshiftoperators/index.md)