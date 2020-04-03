# Local install bundle

See also: [](../bundlemanagement/install.md)

The [`nest.local.install()`](/taskdoc/nest.local.install.html) task allows installing a saker.nest bundle to the local bundle storage. You need to specify the JAR file as the input to the task:

```sakerscript
# Create a bundle JAR
$jar = saker.jar.create(### ... ###)
nest.local.install(
	Bundle: $jar[Path]
)
```

The above example will create a JAR using the [`saker.jar.create()`](root:/saker.jar/taskdoc/saker.jar.create.html) task and install it in the local bundle storage that is configured for the current build execution.