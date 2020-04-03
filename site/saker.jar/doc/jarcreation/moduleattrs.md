# Module attributes

The [`saker.jar.create()`](/taskdoc/saker.jar.create.html) task allows injecting the module main class and module version into the created archive if it contains a `module-info.class` file:

```sakerscript
saker.jar.create(
	ModuleInfoMainClass: example.Main,
	ModuleInfoVersion: "1.0",
)
```

Specifying the above parameters will cause the archiving process to *overwrite* the main class and module version attributes of the `module-info.class` file that is include in the created JAR. If no `module-info.class` entry was added to the JAR, the attributes are **not** injected.

These parameters are similar to the [`jar`](https://docs.oracle.com/javase/9/tools/jar.htm) command `--main-class` and `--module-version` parameters.

Note that the quotes around the value of the `ModuleInfoVersion` parameter is strongly recommended, as otherwise the value may be interpreted as a number.