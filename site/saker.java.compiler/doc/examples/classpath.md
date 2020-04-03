# Classpath

You can add class paths to the compilation configuration in which case the classes on the class path will be visible for the compiled sources:

```sakerscript
saker.java.compile(
	SourceDirectories: src,
	ClassPath: lib/my_library.jar
)
```

In this case the classes in `lib/my_library.jar` will be accessible for the compiled classes in the `src` directory.

## Wildcards

You can add multiple class paths, and use wildcards as well: 

```sakerscript
saker.java.compile(
	SourceDirectories: src,
	ClassPath: [
		lib/*.jar,
		testlib/*.jar
	]
)
```

## Compilation results

You can add other compilation results to the input class path as well:

```sakerscript
$basejavac = saker.java.compile(base)
$libjavac = saker.java.compile(
	SourceDirectories: lib,
	ClassPath: $basejavac
)
saker.java.compile(
	SourceDirectories: src,
	ClassPath: $libjavac
)
```

In this case we compile sources three times. First the `base` directory, which then passed as an input class path to `lib`. The compilation result of `lib` will be used for compiling the sources in `src`.

Note that when passing compilation results as input classpath, the classpaths are resolved **transitively**. Meaning that `src` will see the classes from `lib` and from `base` as well.

## Bundles

See also: [](../javacompile/bundleclasspath.md)

The [`saker.java.classpath.bundle()`](/taskdoc/saker.java.classpath.bundle.html) task allows creating classpath for a given set of saker.nest bundles:

```sakerscript
saker.java.compile(
	SourceDirectories: src,
	ClassPath: saker.java.classpath.bundle(example.bundle-v1.0)
)
```

You can also resolve the dependencies of the specified bundle:

```sakerscript
saker.java.compile(
	SourceDirectories: src,
	ClassPath: saker.java.classpath.bundle(
		nest.dependency.resolve(
			example.bundle,
			Filters: nest.dependency.filter.compile(CompileTransitive: false)
		)
	)
)
```

We used the [compile filter](root:/nest.repository.support/doc/dependencyresolution/filters.html#compile-filter) to appropriately resolve the dependencies for the compilation. This usually results in the API of the bundles being part of the classpath, but implementational bundles are not.