[!section](processorconfig.md)

# Annotation processors

[Annotation processors](https://docs.oracle.com/javase/8/docs/api/javax/annotation/processing/Processor.html) are basically plugins for the Java compilation workflow. They are presented the opportunity to analyze the compiled sources and generate other sources, classes, and resources based on them. They are a very versatile and useful tool for compile-time code generation.

Processors are supported by the [`saker.java.compile()`](/taskdoc/saker.java.compile.html) task and allows them running in an incremental manner. Processors can be specified for the task using the `AnnotationProcessors` parameter:

```sakerscript
saker.java.compile(
	SourceDirectories: src,
	AnnotationProcessors: saker.java.processor(
		ClassPath: lib/my_processor.jar,
		Class: example.MyProcessor
	)
)
``` 

The above is one of the simplest example for adding an annotation processor for the compilation task. The processor with the class name `example.MyProcessor` is loaded from the `lib/my_processor.jar` archive, and is called during the compilation.

## Processor restrictions

Due to the nature of the saker.build system environment and the implementation of the incremental annotation processing, the following restrictions are placed on the processor implementations:

* They **must** only use APIs to access environmental resources that is available to them through the object that they were initialized with.
	* This means that they can only use the [`ProcessingEnvironment`](https://docs.oracle.com/javase/8/docs/api/javax/annotation/processing/ProcessingEnvironment.html) and related classes to read files and other properties of the current compilation environment.
	* They can only use the [`Filer`](https://docs.oracle.com/javase/8/docs/api/javax/annotation/processing/Filer.html) API to read and write files.
	* They **must not** use the [`java.io.File`](https://docs.oracle.com/javase/8/docs/api/java/io/File.html), [`java.nio.file.Files`](https://docs.oracle.com/javase/8/docs/api/java/nio/file/Files.html) or related APIs.
* [Non-aggregating](#non-aggregating-processors) **must** report the elements based on which that they generate the output resources. 

## Processor classification

In order to reduce the work that needs to be done for incremental compilation, we separate the annotation processor type into two category: Aggregating, and non-aggregating.

The definition for an aggregating processor is as follows: *A processor is considered to be Aggregating if there exists an addition-wise modification to the compiled Java classes that causes it to generate different classes or resources.*

What does it mean in practice? Generally, if an annotation processor generates resources by taking information from multiple unrelated parts of the codebase, then it is most likely an aggregating processor. One example for one is the following.

##### Aggregating processor example

Let's imagine a processor, that takes the list of compiled class names, and writes them to a custom output file. If we compile the following classes:

```java
// Bar.java
package example;
public class Bar { }
```

```java
// Foo.java
package example;
public class Foo { }
```

It will generate a text file with the following contents:

```plaintext
// output.txt
example.Bar
example.Foo
```

Why is it aggregating? Because if we add a new class `Baz`, it will generate an output with different contents:

```java
package example;
public class Baz { }
```

Will result in: 

```plaintext
// output.txt
example.Bar
example.Baz
example.Foo
```

We can see that from an incremental perspective, an unrelated Java codebase modification that is not part of the inputs of the `output.txt` generated file may cause it to have different contents.

The aggregating nature of a processor will result in that the whole compilation set will be passed to it in order for it to do its work. It signals the compiler task that it may not perform additional optimizations as that could lead to incorrect output.

All annotation processors are considered to be aggregating unless specified otherwise.

### Non-aggregating processors

If a processor is classified as non-aggregating, then the compiler task is allowed to perform optimizations in order to do less work. Non-aggregating processors generally don't need the whole compilation set as their input, and they can work on a subset of the input.

If a non-aggregating processor creates an output resource, then the contents of that output resource only depends on the specified input elements, and there exists no Java codebase related modification that affects a non-input element, and causes different output resource to be created.

##### Non-aggregating processor example

An example for a non-aggregating processor is one that simply generates a factory class for annotated classes. See the following inputs and outputs:

```java
// Foo.java
package example;
@example.CreateFactory
public class Foo { }
```

The processor will generate the following class for it:

```java
// FooFactory.java (output by processor)
package example;
public class FooFactory {
	public static Foo create() {
		return new Foo();
	}
}
```

We can see that in this case the input and output have a one-to-one relation. Adding a class to the Java codebase will not cause the processor to generate different `FooFactory`. If we add a new class, the compiler task is allowed to **not** pass `Foo` as its input class element, since it was not modified. This results in the processor doing less work in case of incremental scenarios. However, if `Foo` is modified, the processor will be re-run accordingly.

### Non-aggregating restrictions

In order to work properly, non-aggregating processor implementations need to adhere to the following:

* They **must** report the originating elements for the generated sources, classes, and resources. They **must** use the [`Filer`](https://docs.oracle.com/javase/8/docs/api/javax/annotation/processing/Filer.html) API correctly when generating resources.
	* The originating elements should contain **all** Java elements that is an input information used to derive the generated resources. 
	* If they don't report the originating elements, that can cause incremental errors. These error are most likely silently ignored by the compiler task, as it has incorrect information to work with.
* Processors can't generate resources based on the absence of a Java source element. That behaviour would make them aggregating.
* Non-aggregating processors aren't limited to generating resources using a one-to-one relation. They may take Java elements from multiple locations into account.
