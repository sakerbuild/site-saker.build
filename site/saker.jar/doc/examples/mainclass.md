# Specifying main class

See also: [](../jarcreation/manifest.md)

You can specify the main class of your JAR file in two ways. Via the `Main-Class` manifest attribute, and using the module-info of the archive:

```sakerscript
saker.jar.create(
	Manifest: {
		MainAttributes: {
			Main-Class: example.Main
		}
	},
	ModuleInfoMainClass: example.Main
)
```

We recommend specifying both, so you'll have the main class defined independent whether you're using modules or not. If you don't use modules, the `ModuleInfoMainClass` parameter will have no effect.
