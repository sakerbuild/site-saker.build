# Build script style guide

This article states the recommended style for writing build scripts in the SakerScript language.

## Consistent style

Whether or not you take our advices, it is the utmost importance to have a **consistent** style. Strive to format elements in the source code which have the same semantics the same way. 

## Naming

The following naming schemes are recommended for naming different parts of the script.

### Build targets

Build targets should be all lowercase, and single word describing the action that it does. `build`, `compile`, `test`, `export` are all great examples. When the action consists of multiple words, they should be separated by underscore (`'_'`).

```sakerscript
build {
	# ...
}
test_remote {
	# ...
}
```

We don't recommend using build target [name aliases](/doc/scripting/langref/sourcefile/index.md#build-target).

SakerScript doesn't specify visibility rules for build targets. All declared build targets can be called externally. This can cause compatibility errors in larger organizations, as someone who is not familiar with the build script may call build targets that should be internal to the enclosing script.

We recommend prefixing private build targets with underscore (`'_'`) to explicitly signal that it should not be called externally.

```sakerscript
build {
	include(_private_build_target)
}
_private_build_target {
	# ...
}
```

### Parameters

The build target parameters should be formatted in `PascalCase`, with the first letter of each word component being uppercase. Parameters should be declared on their own lines, ending with comma. It may be beneficial to separate the input and output parameters by a single empty line.

Default values for input parameters and assignments for the output parameters are allowed in the parameter list.

```sakerscript
build(
	in SourceDirectory,
	in SomeNumber = 123,
	
	out CompiledBinary,
	out SumValue = $SomeNumber + 456,
) {
	# ...
}
```

The parameter names shouldn't include any phrases that denote their nature of being input or output parameters. Don't name them something like `SourceDirectoryInput` or `InputSourceDirectory`.

The parameter list with the parentheses for a build target should be omitted if there are no parameters. Don't declare a build target with empty parameter list.

### Variables

Variables have 3 scopes based on their accessability. We recommend different naming conventions for all 3 of them.

#### Build target variables

For variables that appear in build targets, we recommend either all lowercase, or `camelCase` naming conventions. They should begin with lowercase characters to clearly distinguish them from being input or output parameters.

```sakerscript
$applesEatenPerDay = 2
$doctorskeptaway = $applesEatenPerDay >= 1
```

Whichever style you choose, make sure to be consistent over the build scripts of your project.

#### Static variables

Static variables are file-level variables which are accessable from every expression in the same build script. See [`static()`](/doc/scripting/builtintasks/static.md) for more information about them.

Static variables which contain constant configuration data should be named in `UPPER_SNAKE_CASE` format. If the value of the variable is calculated dynamically, they should have the same naming format as [](#build-target-variables). 

```sakerscript
static(DEPENDENCY_VERSION) = 1.0
static(commonResult) = common.task()
build {
	example.load.dependency("my.dependency-v{ static(DEPENDENCY_VERSION) }")
}
```

#### Global variables

Global variables are named build execution level variables that are shared with **all** of the build scripts, regardless of the language of the associated script. See [`global()`](/doc/scripting/builtintasks/global.md) for mor information about them.

They should be named in `<identifier>.UPPER_SNAKE_CASE` format. The `<identifier>` should uniquely identify the company and/or the project that the variable is present in.

Global variables should only contain constant configuration data, and their value should not be dynamically computed.

```sakerscript
global(company.hello.world.DEPENDENCY_VERSION) = 1.0
build {
	example.load.dependency("my.dependency-v{ global(company.hello.world.DEPENDENCY_VERSION) }")
}
```

This naming conventions applies for all scripting languages. It also helps avoiding conflicts when build scripts from other sources are included.

### Task parameters

The following mainly applies for task implementation developers. Task parameters should be named in `PascalCase`, similarly to build target parameters.

```sakerscript
example.compile.sources(
	SourceDirectory: src, 
	Architecture: x86
)
```

If the task declares unnamed parameters, the expected value for that parameter should be straight forward to understand based on the name or behaviour of the declaring task.

## Calling tasks

When task invocations are declared, they might take multiple parameters. With multiple parameters, the line of the declaration may be too long, or otherwise be chaotic.

We recommend breaking up the parameters of a task invocation into multiple lines in the following way:

```sakerscript
# single unnamed parameter, no breaking
include(build)
# unnamed parameter, and one single named parameter.
# breaking is optional
include(build, Path: saker.build)
# more than 1 named parameter, the declaration should be broken up
# each parameter on a new line indented with 1 tab
# all parameters should have a separator comma after them
# closing parentheses indented the same amount as the task name
include(
	build, 
	Path: saker.build, 
	SourceDirectory: src, 
	Architecture: x86,
)
```

## Literals

Simple literal declarations should be on a single line without breaking up.\
Lists may be broken up when it becomes too long on a single line. If a list is broken up, then all of the elements should be on a separate line.\
Maps can be on a single line if they contain at most 1 entry. In other cases they should be broken up with 1 entry on each line.

```sakerscript
$simple = abcd123
$simplelist = [1, 2, 3]
$biglist = [
	1,
	2,
	3,
]
$simplemap = { Key: value }
$bigmap = {
	Key: value,
	ListKey: [1, 2, 3],
	BigListKey: [
		1,
		2,
		3,
	],
	MapKey: { Key: value },
	BigMapKey: {
		TheValue: abcd123,
	},
}
```

When lists or maps are broken up, all of their elements/entries should end with a comma.

## Compound literals

Compound literals are the strings which are declared in quotes in the language. (See [](/doc/scripting/langref/literals/index.md#compound-string-literals).)

Writers of a build script are recommended to use simple unquoted literals when possible, and use quotes when they contain special characters. They should not actively search for literals that could be unquoted, meaning that if a literal has been used in quotes, they may remain in quotes.

## Whitespace

Spaces should be used in places of operators or other kind of separator source elements.

```sakerscript
# spaces around operators
$sum = 123 + 456
# spaces around map braces and after key ':'
$map = { Key: value }
# spaces after commas, but not necessarily around list brackets
$list = [1, 2, 3]
# spaces around if-else braces
if $condition {
} else {
}
#spaces around braces and result expression ':'
foreach $item in $iterable {
} : [$item]
```

## Indentation 

Indentation should be done using tabs. Tabs are recommended to be displayed with a width of 4 spaces.

Every source code element that is enclosed in an other construct should be indented with 1 more tab than its enclosing element. These are build target parameters, map entries, list elements, task parameters, `foreach` local variables, expression statements, and others.

```sakerscript
# indent local declarations by 1, on the same level as expressions
foreach $item in $iterable
	with $local {
	# ...	
}
```

## Ordering

The elements in the source files should have the following order:

1. Global variable assignments
2. Static variable assignments
3. Global expressions
4. Publicly invokable build targets
5. Internal build targets

```sakerscript
global(global.VAR) = 123
static(STATIC_VAR) = 456
global.expression.task()
build {
	# ...
}
_private_build_target {
	# ...
}
```

Expressions in build targets or among the global expressions should be ordered in a way that it can be semantically followed. Generally this is the order that the build process would do if the execution was sequential. (When possible to declare them this way.)

We strongly advise against declaring expressions in the following order:

```sakerscript
example.compile.sources(SourceDirectory: $sourcedir)
$sourcedir = src
```

You see that the `$sourcedir` variable is assigned *after* it is being used. This can be counterintuitive for the reader. We recommend declaring the inputs of an expression before they are being used in that expression.
