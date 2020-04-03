# Compiler parameters

You can pass parameters which are directly interpreted by `javac`:

```sakerscript
saker.java.compile(
	SourceDirectories: src,
	Parameters: [ --release, 8 ],
)
```

The above `--release 8` arguments will be passed to `javac` in order to execute the compilation for JDK 8. This can be useful when cross-compiling.

For other parameters that may be passed to `javac`, see the `javac` tool documentation of the associated JDK:

* [`javac` 8](https://docs.oracle.com/javase/8/docs/technotes/tools/windows/javac.html)
* [`javac` 9](https://docs.oracle.com/javase/9/tools/javac.htm)
* [`javac` 10](https://docs.oracle.com/javase/10/tools/javac.htm)
* [`javac` 11](https://docs.oracle.com/en/java/javase/11/tools/javac.html)
* [`javac` 12](https://docs.oracle.com/en/java/javase/12/tools/javac.html)
* [`javac` 13](https://docs.oracle.com/en/java/javase/13/docs/specs/man/javac.html)

Some specified parameters may be specially handled by the compiler task. E.g. the parameter `--enable-preview` is only available from JDK 11. If it is specified for compilations running on earlier versions, it will be silently removed.