# Data structures

There are no type system in SakerScript. However, you can declare composite data in the format of maps, lists, and literals:

```sakerscript
$map = {
	Key: value,
	ListKey: [
		item1,
		item2,
	]
}
$list = [1, 2, 3]
$simpleliteral = abcd1234
$compoundstring = "interpolated string number: { 2 * 3 }"
```

These structures can be used in various ways to configure the task invocations. It is mostly dependent on the implementation and purpose of the task, but the following example can shine some light on it:

```sakerscript
example.compile.sources(
	Sources: [
		{
			Directory: src,
			Sources: **/*.cpp,
		},
		{
			Directory: test,
			Sources: **/test_*.cpp,
		},
	],
	LinkLibraries: [
		lib1.lib,
		lib2.lib,
	],
)
```

The above fictional task would compile the sources with the `.cpp` extension the the `src` directory, and the sources in `test` directory that begin with `test_` and end with `.cpp`. It would also link the libraries `lib1.lib` and `lib2.lib` to the resulting binary.

Note that the data structures in the language are immutable, they cannot be modified after they've been constructed.

The data structures can be used in other various ways, see [](/doc/scripting/langref/literals/index.md) for more information.