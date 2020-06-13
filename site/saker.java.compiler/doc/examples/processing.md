# Annotation processing

We recommend reading [Annotation processor configuration](../javacompile/processorconfig.md) before this article. (Some of the examples are similar to the ones in that article.)

To simply load an annotation processor from a classpath and use it in a compilation:

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

The above will result in the `example.MyProcessor` being used when compiling the sources in `src`.

## Options

To pass options for the used annotation processors:

```sakerscript
$processor = "__TOKEN__"# ...
saker.java.compile(
	SourceDirectories: src,
	AnnotationProcessors: $processor,
	AnnotationProcessorOptions: {
		example.MyProcessor.option: abc
	}
)
```

This will pass the `example.MyProcessor.option` option with the `abc` value to the invoked processor(s). Processors can also be configured with processor-private options, see the example [here for more information](../javacompile/processorconfig.md#options).

## Input locations

Input locations may be specified for annotation processors to use. They will be able to read files from the specified locations:

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

More information [here](../javacompile/processorconfig.md#input-locations).
