# Simple compilation

To simply compile all source files that end with `.java` under the `src` subdirectory of the working directory:

```sakerscript
saker.java.compile(src)
```

The task allows specifying multiple source directories as well:

```sakerscript
saker.java.compile([
	src_1,
	src_2
])
```

The above parameters correspond to the `SourceDirectories` parameter. The following is exactly same as the above:

```sakerscript
saker.java.compile(SourceDirectories: src)
saker.java.compile(
	SourceDirectories: [
		src_1,
		src_2
	]
)
```