# Sorted iteration

Among others, the [`ObjectUtils`](/javadoc/saker/util/ObjectUtils.html) provides functions for iterating over sorted collections. There are many use-cases when two collections need to be iterated over, presuming that they are ordered the same way. You can not do this in a simple way, as that requires multiple for loops and temporary collections.

The `iterateOrdered` and `iterateSorted` functions can help iterating over two collections/maps:

```java
SortedMap<String, Integer> first;
SortedMap<String, Integer> second;
ObjectUtils.iterateSortedMapEntries(first, second, (key, left, right) -> {
	if (left == null) {
		// first map doesn't contain entry for the key
	} else if (right == null) {
		// second map doesn't contain entry for the key
	} else {
		// both map contain entry for the key
	}
});
```

The function will iterate both maps the same time, and use their comparators to iterate them in ascending order of their keys. The iteration is O(n + m), where n is the size of the first map, and m is the size of the second. The specified lambda will be called for each present key in both maps.

Using these iteration functions may be significantly faster than performing lookups in a loop. An important use-case that we've found for this is when calculating the deltas for the build tasks in the [saker.build system](root:/saker.build/index.html). When large number of files may be present, the sorted iteration is much faster than doing lookups for each file we want to check the changes for.
