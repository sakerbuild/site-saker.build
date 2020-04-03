# Subscript

The subscript operator is used to retrieve the specified component of the subject expression. The subscript operator can be used on [lists](../../literals/index.md#lists), [maps](../../literals/index.md#maps), and arbitrary objects from external sources.

The behaviour of the operator is based on the subject expression value.

* If the subject is a list, then the index expression will be interpreted to be a zero-based index in the subject list.
* If the subject is a map, then the index is interpreted to be a string key into the subject map, and will retrieve the corresponding value for it. The same applies if the subject is a result object from a build target invocation.
* In any other case, the language implementation will try to retrieve an uniquely identifiable data from the subject object. In the saker.build system implementation it will search for a no-arg method with the name of `get<index>()` or `get_<index>()` and call it appropriately. 

The subscript operator can use partially computed subject objects, meaning that it may retrieve fields of an objects for which the corresponding value expression is still being evaluated by the interpreter.

The subscript operator will throw a runtime exception if the value for the given index is not found. In case of maps, it won't result in `null`, but in an error if the key is not found. Note however, if subscripting occurs on an external object, it still can result in `null`.

```sakerscript
# example datas
$list = [1st, 2nd, 3rd]
$map = {
	NumKey: 123,
	ListKey: [1, 2, 3],
	MapKey: {
		SecondNum: 456
	},
}

# evaluates to 1st
$list[0]
# Error, out of bounds
$list[3]

# evaluates to 123
$map[NumKey]

# evaluates to [1, 2, 3]
$map[ListKey]
# evaluates to 1
$map[ListKey][0]

# evaluates to the map in MapKey
$map[MapKey]
# evaluates to 456
$map[MapKey][SecondNum]

# throws a runtime error
$map[NonExistent]
```
