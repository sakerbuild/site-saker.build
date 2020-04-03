# Literals

The language allows the usage of literals which are the basic instances of data in the script. Literals are such as numbers, strings, lists, and maps. These structures may be familiar from other languages.

All data structures defined in the language are immutable after construction.

## Numbers

The language defines intergral and floating point number literals.

### Integers

The integral literals can be provided in either base-10 numeric format or hexadecimal format.

```sakerscript
# simple integer
$int = 123456789
# invalid integer
$invalid = 0452

# hexadecimal format
$hexa = 0x012abCDf
```

The language doesn't support integers in the base-ten numeric format that start with `0`. This is in order to avoid confusion that comes with the Java language octal number syntax. (See [JLS 3.10.1](https://docs.oracle.com/javase/specs/jls/se7/html/jls-3.html#jls-3.10.1)) This restriction doesn't apply to the hexadecimal format.

### Floating point numbers

The floating point literals can be declared in the same format as specified by the Java language. (See [JLS 3.10.2](https://docs.oracle.com/javase/specs/jls/se7/html/jls-3.html#jls-3.10.2)) This can make easier to represent number between language boundaries, and provides a more straightforward implementation of the language.

```sakerscript
# simple double
$double = 1.23
# exponential format
$exponential = 1234.5E-6

# not a number
$nan = NaN
# string "NaN" that's not interpreted as a number literal
$nanstr = "NaN"
```

### Precision

The definition doesn't include any precision requirements in regards of the numbers, but the backing implementation of the language should define that. It can be expected that all number literals have a minimum precision of 64 bits. 

The current implementation for the build system allows arbitrary precision integrals (via `BigInteger` if necessary), but limits the floating point representation to 64 bits (via `double`).

## String literals

String literals have two types in the language. Simple literals that are not handled specially and parsed as-is, and compound literals which provide additional features such as escaping, interpolation, and multi-line declaration.

### Simple string literals

Simple string literals are just continuous characters in the source code. They are only bounded by whitespace and special characters. Their format is that they match the following regular expression: `[^,\(\)\[\]\{\}:;\s]+`.

```sakerscript
# simple string
$simple = some_string_value
# string for a file path
$filepath = relative/path/to/a/file
# some base64 data
$base64 = VE9ETyBzb21ldGhpbmcgZnVubnk=
# special characters
$special = some+string!containing?special-characters
```

The simple literals are parsed until any significant control characters are encountered, which are specified in the above regexp. This means that if developers need to use these literals in complex expressions that use operators, the literals need to be separated from the used operator via whitespace.

Note that initially number literals are also parsed as simple string literals, but they are later converted to their appropriate numeric representation if possible.

### Compound string literals

Compount string literals allow reified representation of data. They are in a format of quoted characters that support escaping, whitespace, multi-line declaration, and interpolation.

```sakerscript
# simple compound string: 
#quoted string
$simple = "quoted string"

# escape special characters:
$escaped = "escapes:\ \b\t\n\f\r\"\'\\"

# unicode:
#XY
$unicode = "\u0058\u0059"

# octal:
#X$
$octal = "\130\44"

# interpolate:
#money: 56 USD
$interpolated = "calculated: { 7 * 8 } USD"

# multi-line:
#first
#second
#    indented
# same as "first\nsecond\n    indented"
$multi = "first
second
    indented"
```

Character escaping works similarly as in the Java language. (See [JLS 3.10.6](https://docs.oracle.com/javase/specs/jls/se7/html/jls-3.html#jls-3.10.6)) It supports escaping special characters, quotes, backslash, and accepts declaration format in octal and unicode representation. Additionally to this, the language allows escaping opening braces (`'{'`) to allow compatibility with interpolation and a simple space (`' '`) in order to provide compatibility for using strings from external sources.

The unicode escape sequence expects 4 hexadecimal characters that specify the described unicode character. The octal escape sequence expect the numbers in a base-8 numeric format with at most of 3 numbers.

The compound string literals will end when an unescaped `'"'` character is encountered. This allows multi-line strings to be declared. The indentation of multi-line strings will not be stripped. The line endings of such strings will be normalized to a single `'\n'` character.

String interpolation can be used by including arbitary expressions enclosed by curly brace characters. The interpolated expression will be evaluated, and a string representation of it will be included in the string at the corresponding position.

Compound string literals are always interpreted as string types, and their final value is never handled specially by the language.

## Special literals

Some literals are handled specially by the language. Booleans and `null` are such and they are described in the following sections.

### Booleans

Booleans are logical values representing the state of `true` or `false`. They are specially handled by the language, and simple literals that match these states in a case-insensitive manner will be converted to them.

```sakerscript
# true boolean
$t = true
# also true
$at = TrUe

# false boolean
$f = false
# also false
$af = FalSE

# not a boolean
$nb = some_literal
# also not a boolean, but a string
$anb = "true"
```

### `null`

The `null` value in the language represents the absence of some data. Their main reason for its existence is the compatibility with the extension mechanism, as data types from external sources can be present in the language.

Generally, you should not be using `null`s in your build script, but handle exceptional cases specified the task extensions of the language.

Similar to booleans, `null`s are also interpreted in a case-insensitive manner.

```sakerscript
# set null to a variable
$var = null
# also null, as it is interpreted in a case-insensitive manner
$var2 = NuLL
$var3 = NULL
# not null, but a string literal
$notnull = "null"
```

## Complex structures

There are many use-cases when structured data needs to be represented in a language. SakerScript supports lists and maps as the base of these representations.

### Lists

Lists contain a sequence of elements in the order specified during construction. They can be declared by using square brackets (`[ ]`).

```sakerscript
# simple list
$list = [1, 2, 3]
# nested list
$nested = [[1, 2], [3, 4]]

# mixed type list
$mixed = [1, "string", [item1, item2]]

# empty list
$empty = []
# extra commas
# same as [1, 2]
$commas = [, 1,, 2, ]
```

Any kind of elements can be declared for lists, there is no requirement in regards to it. The lists have a fixed size after construction, and modifying later is not possible.

Any extraneous commas in the declaration of a list are ignored.

It is generally recommended to have the elements of a list the same data structure, rather than mixing different kinds of data in one.

Lists can be used in [`foreach` expressions](../foreach/index.md), and can be used as the subject of the [subscript operator](../operators/subscript/index.md).

### Maps

Maps (also known as dictionaries in other languages) are a storage of key-value pairs. The keys in a map are unique strings, and declaring a map that has multiple values for the same key is considered an error. Maps can be declared by using the curly braces (`{ }`).

```sakerscript
# example map
$example = {
	Number: 123,
	List: [
		1, 
		2,
	],
	SubMap: {
		String: "str",
	},
}

# computed key
$computed = {
	"Key{ 2 * 3 }": 6,
}

# empty map
$empty = { }
```

The keys and values of a map declaration are general expressions, they are not subject to additional requirements. However, when the keys are evaluated, they are converted to a string representation. If there are multiple keys with the same name, an error will be thrown during evaluation.

Any extraneous commas in the declaration of a map are ignored.

It is generally recommended to only use simple literals for the keys of a map, and use computed keys rarely when necessary.

Maps can be used in [`foreach` expressions](../foreach/index.md), and can be used as the subject of the [subscript operator](../operators/subscript/index.md).

The order of keys in the maps are unspecified and implementation dependent. Users should not rely on map objects to have any specific iteration order of their key-value entries.