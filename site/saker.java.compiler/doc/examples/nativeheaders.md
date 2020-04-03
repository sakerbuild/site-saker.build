# Native headers

The [`saker.java.compile()`](/taskdoc/saker.java.compile.html) task can generate the native headers for your classes if you specify the `GenerateNativeHeaders` parameter. By default, it won't generate the headers.

```sakerscript
$javac = saker.java.compile(
	SourceDirectories: src,
	GenerateNativeHeaders: true
)
$javac[HeaderDirectory]
```

The path to the header directory can be retrieved using the `HeaderDirectory` field. It can be passed to other tasks as an include directory.

