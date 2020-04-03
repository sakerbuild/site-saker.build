# Adding services

See also: [](../jarcreation/services.md)

You can add service declarations to your archive using the `Services` parameter:

```sakerscript
saker.jar.create(
	Services: {
		javax.annotation.processing.Processor: example.MyAnnotationProcessor
	}
)
```

The above will cause the archive to contain a service declaration file with the path `META-INF/services/javax.annotation.processing.Processor` that declares the `example.MyAnnotationProcessor` implementation class.

You can specify multiple services and multiple implementation names as well:

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
