# Compilation identifier

See also: [`CompilationIdentifier` JavaDoc](/javadoc/saker/compiler/utils/api/CompilationIdentifier.html)

A compilation identifier consists of dash (`'-'`) separated name compontents. Its purpose is to uniquely identify a given compilation pass or operation in the current build execution. The compilation identifiers are human-readable.

When a compilation task is run, it is run with an assigned compilation identifier. This identifier will uniquely identify the output location of the compilation, and is associated with a build task in the build system. This ensures that only a single compilation task will produce its outputs for the given location, and the tasks won't overwrite each others outputs.

Let's look at an example, with Java compilation:

```sakerscript
saker.java.compile(
	SourceDirectories: src/main,
	Identifier: main
)
saker.java.compile(
	SourceDirectories: src/lib,
	Identifier: lib
)
```

In the above, we assigned different compilation identifiers for the different compilation tasks. They will run concurrently, and produce their outputs for the associated location. (See [](root:/saker.java.compiler/doc/javacompile/basics.html#compilation-output).)

The compilation identifiers also ensure that no differently configured compilation tasks will run with the same identifier:

```sakerscript
saker.java.compile(
	SourceDirectories: src/main,
	Identifier: main
)
saker.java.compile(
	SourceDirectories: src/lib,
	Identifier: main
)
```

The above will cause a build error thrown by the saker.build system, as different compilation tasks are started for the same compilation identifier.

**Note** that the above behaviours may be dependent on the actual tasks that use the compilation identifiers.

## Format

Compilation identifiers consist of name parts separated by dashes. The names may consist of characters `a-z`, `A-Z`, `0-9`, `_`, `.`, `()`, `[]`, `@`. When a compilation identifier is constructed, it is normalized to lowercase letters. The name parts are deduplicated, and not ordered. Empty name components, preceeding and trailing dashes are omitted.

Some compilation identifier examples:

```plaintext
main
test
lib-debug
lib-release
lib-debug-x64
lib-debug-x86
```

Note that all of the following compilation identifiers are considered to be the same:

```plaintext
lib-debug-x64
x64-lib-debug
-x64---lib--debug--
LIB-X64-dEbUG
```

## Operations

Compilation identifiers have the following operations defined on them. These operations are usually performed by the build tasks, and not in the build script.

#### Concatenation/union.

The name components of the operand compilation identifiers are merged, and deduplicated. A single compilation identifier is the result that contains all of the operant name parts.

E.g. `lib-x64` concatenated with `lib-debug` results in `lib-x64-debug`.

#### Subset

Checks whether all of the name components of an identifier are present in another. This operation is often performed then [compiler option merging](optionmerging.md) is being done.

Some examples:

* `main` is a subset of `main`
* `main` is a subset of `main-debug`
* `lib-x64` is a subset of `lib-x64-debug` (and other variants like `lib-debug-x64`, order doesn't matter)
* `lib-release` is **not** a subset of `lib-x64-debug`
