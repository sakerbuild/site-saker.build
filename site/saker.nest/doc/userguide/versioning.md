# Versioning

The version numbers used in the repository are defined in a specific format. A version number consists of one or more dot (`'.'`) separated non-negative numbers. Version numbers are non-ambiguous and strictly ordered.

The first recognized version number is `0`. This is considered to be smaller than all other version numbers from an ordering perspective. When comparing version numbers, each component is compared individually, and the one that contains the lesser number components are ordered first. `.0` components at the end of a version number is **not** omitted. Some examples in strictly ascending order:

```plaintext
0
0.0
0.1
0.1.0
0.9
0.10
0.10.0
0.11
1.0
1.1
1.1.0
1.2
1.2.3.4.5
1.2.4
2.0
3
3.0
3.1
4
4.1
```

You can see that the `.0` ending of versions are not omitted, and e.g. `1.1.0` is always ordered after `1.1` from a versioning perspective. One can also see that if a package consumer declares a dependency with a soft upper bound, then a minor bugfix release can also be published that satisifes such dependencies.

If a version number is prefixed with the character `'v'`, then it is considered to be a *version qualifier*. They are used in bundle names to signal the [release version](index.md#names).

## Version ranges

A version range is a specification of allowed version numbers for a given use-case. They are often used when declaring [bundle dependencies](../devguide/dependencies.md). When a specification is interpreted as a *version range*, it can consist of the following elements:

* **Number**: A version number as defined above. E.g. `1.2.3`\
Numbers without any enclosing range declaration will allow any versions that start with the given number. E.g. the input `1.2` allows `1.2`, `1.2.0`, `1.2.1`, and any following version numbers up until `1.3`.
* **Range**: Two version numbers separated by comma (`','`) enclosed in either parentheses (`'('` and `')'`) or brackets (`'['` and `']'`). The kind of enclosing characters may be used together. The enclosing characters correspond to the same semantics as in the notation used by intervals in mathematics. (Parentheses for open ended (exclusive) ranges, and brackets for closed (inclusive) ranges.)\
The right side of the range must be greater than the left side.\
E.g. `[1, 2)` includes any version starting from `1` and is smaller than `2`.
* **Singular range**: A range declaration that only contains one version number. It can have three formats:

	* `[1.0)`: meaning versions at least `1.0`, without any upper bound.
	* `(1.0]`: meaning versions at most `1.0`, without any lower bound. (The range is inclusive for `1.0`.) This is semantically same as `[0, 1.0]`, as the version `0` is the first one in order.
	* `[1.0]`: meaning exactly the version `1.0`
	
	Note that a singular version range with parentheses on both end is illegal.
* **Union relation**: Any of the components can be enclosed in curly braces (`'{'` and `'}`) and separated by vertical bars (`'|'`). This declaration will enable versions matched by any of its compontents. E.g. `{[1.0] | [2.0]}` matches only the versions `1.0` and `2.0`. Note that an union declaration without any components is considered to include *no versions*.
* **Intersection relation**: Any of the components can be enclosed in intersection relation with each other. The `'&'` character can be used to require that all parts of the input is satisfied. This relation exists for completeness of the version range format, and we haven't found a significant use-case for it as of yet. In general, intersections can be represented in a range based way more appropriately.

Some examples:

* `1.0`: Includes any version greater or equal to `1.0` and less than `1.1`. Semantically same as `[1.0, 1.1)`.
* `{1 | 3}`: Includes versions starting with `1` or `3`, but doesn't include versions with other starting components. \
Included examples: `1`, `1.0`, `1.1`, `3`, `3.2`\
Not included examples: `2`, `2.0`, `4.0`
* `{}`: Doesn't include any versions. Unsatisfiable.
* `(1.1, 1.4)`: Includes versions greater than `1.1` and less than `1.4`. \
Included examples: `1.1.0`, `1.1.1`, `1.2`, `1.3.9`,
`1.3.9.0`\
Not included examples: `1.0`, `1.1`, `1.4`, `1.4.0`
* `{1.0}`: Same as `1.0`.
