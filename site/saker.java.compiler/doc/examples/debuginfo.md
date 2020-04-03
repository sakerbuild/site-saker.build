# Debug information

The Java compiler may include debugging information in the generated class files for easier debugging and more readable exception stack traces.

By default, the [`saker.java.compile()`](/taskdoc/saker.java.compile.html) task will include *all* debugging information in the class files. (`-g` flag)

You can configure it using the `DebugInfo` parameter.

```sakerscript
saker.java.compile(
	SourceDirectories: src,
	DebugInfo: [
		source, lines
	]
)
```

The above will include the source and line debugging information, but not the local variables.

You can turn debugging information off by specifying an empty list, or `none`:

```sakerscript
saker.java.compile(
	SourceDirectories: src,
	DebugInfo: []
)
saker.java.compile(
	SourceDirectories: src,
	DebugInfo: none
)
```

If you don't want to include debugging information, you may want to strip the [parameter names](parameternames.md) as well.