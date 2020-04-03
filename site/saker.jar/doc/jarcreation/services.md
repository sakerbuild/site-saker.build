# Services

The [`saker.jar.create()`](/taskdoc/saker.jar.create.html) task allows specifying service classes that are included in the meta information of the created Java archive. For information about the service configuration files see the [Service provider specification](https://docs.oracle.com/javase/8/docs/technotes/guides/jar/jar.html#Service_Provider).

The `Services` parameter of the task can be used to specify additional services for the archive:

```sakerscript
saker.jar.create(
	Services: {
		example.Service: example.ServiceImplementation
	}
)
```

The above will result in the resulting archive containing an entry at the path `META-INF/services/example.Service` with the contents of:

```plaintext
example.ServiceImplementation
```

You can specify multiple services for the parameter as well:

```sakerscript
saker.jar.create(
	Services: {
		example.one.Service: [
			example.one.FirstServiceImplementation,
			example.one.SecondServiceImplementation
		],
		example.two.Service: example.two.ServiceImplementation
	}
)
```

In which case the service file for `example.one.Service` will contain both specified implementation class names.

When specifying services via the task parameter, the service class names will be merged with any existing service files that were added to the archive.
