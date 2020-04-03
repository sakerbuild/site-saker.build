# Compiler options

The [`saker.java.compile()`](/taskdoc/saker.java.compile.html) task allows dynamic option merging based on the identifier of the compilation pass. It can be used to define the compilation parameters in one place, and then later can be applied to the compilation tasks selectively.

Let's look at an example where [cross-compile](crosscompile.md) different versions of the same codebase:

```sakerscript
$options = [
	{
		Identifier: jdk8,
		SDKs: {
			Java: saker.java.sdk(8)
		}
	},
	{
		Identifier: jdk9,
		SDKs: {
			Java: saker.java.sdk(9)
		}
	},
]
saker.java.compile(
	SourceDirectories: src,
	Identifier: main-jdk8,
	CompilerOptions: $options
)
saker.java.compile(
	SourceDirectories: src,
	Identifier: main-jdk9,
	CompilerOptions: $options
)
```

In the above build script we can see that there are two Java compilations being done. One with the `main-jdk8` identifier, and one with the `main-jdk9` identifier. We also specify some options in the `$options` variable that we pass to both compilation tasks using the `CompilerOptions` parameter. The options specify the JDK that should be used for compilation for `jdk8`, and `jdk9`.

When the [`saker.java.compile()`](/taskdoc/saker.java.compile.html) task receives the `CompilerOptions` input, it will analyze its contents, and merge the configurations specified in the options when applicable. The merging is done by checking if the `Identifier` parts in the option declaration is contained in the `Identifier` parameter of the compilation task. The inclusions is determined based on the dash (`'-'`) separated components of the identifiers.

The above example is works **exactly** the same way as the following:

```sakerscript
saker.java.compile(
	SourceDirectories: src,
	Identifier: main-jdk8,
	SDKs: {
		Java: saker.java.sdk(8)
	}
)
saker.java.compile(
	SourceDirectories: src,
	Identifier: main-jdk9,
	SDKs: {
		Java: saker.java.sdk(9)
	}
)
```

The outputs of the compilations will be in separate output directories.

This compiler options merging facility can be used with other input parameters for the compilation tasks as well.

Note that the initial example could be simplified more using the `foreach` structure:

```sakerscript
$options = # ... same as above ...
foreach $jdk in [jdk8, jdk9] {
	saker.java.compile(
		SourceDirectories: src,
		Identifier: "main-{ $jdk }",
		CompilerOptions: $options
	)
}
```

This can make easier to add new flavors of your compilation passes.
