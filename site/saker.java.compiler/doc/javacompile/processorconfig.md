# Configuration

Annotation processors can be configured in various ways. The [`saker.java.compile()`](/taskdoc/saker.java.compile.html) task allows both global and processor-private configuration parameters.

## Loading processors

The annotation processors can be loaded in various ways. We provide the `saker.java.processor()` task that loads an annotation processor from the specified classpath. The output of this task can be passed as the input to the `AnnotationProcessors` parameter of the compilation task.

```sakerscript
$processor = saker.java.processor(
	ClassPath: lib/my_processor.jar,
	Class: example.MyProcessor
)
saker.java.compile(
	SourceDirectories: src,
	AnnotationProcessors: $processor
)
```

Note that the `saker.java.processor()` task output can be reused as input to multiple different compilation tasks.

Annotation processor authors may provide different mechanisms for loading the processors. Please consult the documentation of your chosen annotation processor for more information. 

## Aggregating

One of the most important configuration for annotation processors is whether or not they're [aggregating](processors.md#processor-classification) in nature. This can be done by either the task that loads the processor, or by manually overriding it when passing to the compilation task:

```sakerscript
# specifying when the processor is loaded
saker.java.compile(
	SourceDirectories: src,
	AnnotationProcessors: saker.java.processor(
		ClassPath: lib/my_processor.jar,
		Class: example.MyProcessor,
		Aggregating: true
	)
)
``` 
```sakerscript
# overriding when passed to the compilation task:
saker.java.compile(
	SourceDirectories: src,
	AnnotationProcessors: {
		Processor: saker.java.processor(
			ClassPath: lib/my_processor.jar,
			Class: example.MyProcessor
		),
		Aggregating: true
	}
)
```

Both above solutions have its advantages, we recommend using the first one, by specifying alongside the loading parameters. If you don't have control over the loading configuration of the processor, you can use the second one to *override* the configuration.

## Options

The annotation processors can work with arbitrary key-value string based input options. These are passed via the [`ProcessingEnvironment`](https://docs.oracle.com/javase/8/docs/api/javax/annotation/processing/ProcessingEnvironment.html#getOptions--).

The compilation task allows specifying both global and processor-private options:

```sakerscript
saker.java.compile(
	SourceDirectories: src,
	AnnotationProcessors: [
		{
			Processor: ### ... ### ,
			# options only available to this processor
			Options: {
				example.first.processor.option1: 123
			}
		},
		{
			Processor: ### ... ### ,
			# options only available to this processor
			Options: {
				example.second.processor.option1: 456
			}
		}
	],
	# options available to all used processors
	AnnotationProcessorOptions: {
		example.global.option: abc
	}
)
```

In the above example we define multiple annotation processors to be used with the compilation task. The first processor will have the input options named `example.first.processor.option1` and `example.global.option`, while it does **not** receive the option `example.second.processor.option1`. The same semantics apply to the second processor.

## Input locations

The annotation processors are forbidden to use the [`java.io.File`](https://docs.oracle.com/javase/8/docs/api/java/io/File.html), [`java.nio.file.Files`](https://docs.oracle.com/javase/8/docs/api/java/nio/file/Files.html) or related APIs. In order to provide an alternative for that, developers can specify named input locations for the processors:

```sakerscript
saker.java.compile(
	SourceDirectories: src,
	AnnotationProcessors: {
		Processor: ### ... ### 
	},
	ProcessorInputLocations: {
		WORKING_DIRECTORY: "",
		PROC_RES: processor_resources
	},
)
```

In the above scenario, processor implementations will have access to the files in the current working directory by using the `WORKING_DIRECTORY` location, and to the resources in the `processor_resources` subdirectory using the `PROC_RES` location.

Processor implementations can use the [`StandardLocation.locationFor()`](https://docs.oracle.com/javase/8/docs/api/javax/tools/StandardLocation.html#locationFor-java.lang.String-) function to retrieve a location to use with the [`Filer`](https://docs.oracle.com/javase/8/docs/api/javax/annotation/processing/Filer.html) API. Note that writing files to these locations are forbidden.
