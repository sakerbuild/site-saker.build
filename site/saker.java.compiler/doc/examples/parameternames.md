# Reflection parameter names

The Java compiler may strip the parameter names of the generated class files. The [`saker.java.compile()`](/taskdoc/saker.java.compile.html) task automatically instructs the `javac` backed whether or not to include the parameter names in the class files.

The default is to include them.

You don't need to pass the `-parameters` flag to the compiler by default.

However, you can turn off the inclusion of parameter names by specifying the `ParameterNames` parameter:

```sakerscript
saker.java.compile(
	SourceDirectories: src,
	ParameterNames: false
)
```

The above compilation task will result in generating class files that do **not** include method parameter names in them.

If you need to turn off parameter names, you may also want to consider stripping the [debug information](debuginfo.md).