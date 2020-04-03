# Local installing

See also: [](root:/saker.nest/doc/userguide/localstorage.html)

When developing saker.nest bundles, you may want to use them. Installing them to the local bundle storage can be one way of doing this. The [`nest.local.install()`](/taskdoc/nest.local.install.html) task performs bundle installing to the specified local bundle storage.

The task take one or more bundles which are to be installed to the local bundle storage:

```sakerscript
# Create a bundle JAR
$jar = saker.jar.create(### ... ###)
nest.local.install(
	Bundle: $jar[Path]
)
```

The above example will create a JAR using the [`saker.jar.create()`](root:/saker.jar/taskdoc/saker.jar.create.html) task and install it in the local bundle storage that is configured for the current build execution.

**Important** to note that any information in the currently installed bundle will **not** be accessible in the current build execution. If the bundle contains a build task, you will not be able to call it in the same build execution that installed the bundle itself.

If you have multiple local storages configured for the current build execution, use the `StorageName` parameter to specify the install target storage.
