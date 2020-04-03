# IDE configuration

During the executions of tasks, they may report various data to the build system in order to provide configuration related data to Integrated Development Environments (IDE). These datas can be used by external consumers (most likely IDE plugins) to properly configure the associated project in order to provide features that enhance code editing to the user. (E.g. content assist)

IDE configurations can be reported by tasks using the `TaskContext.reportIDEConfiguration` method. The build system will store these configurations and make them available for IDE plugins and others when requested.

IDE configurations hold information related on how an IDE project should be configured. They can consist of numbers, strings, collections, and string keyed map compositions of theirs. \
They have a string type which is an arbitrary unique string that defines the schema of the object which determines how external sources are supposed to interpret the object. They also have an identifier which is an user readable string that is displayed to the user when possible IDE configurations are listed.

The configurations can be reported in the form of an `IDEConfiguration`, and we recommend using the `SimpleIDEConfiguration` implementation of it.

One possible example for an IDE configuration for Java compilation is the following:\
<small>The following format is arbitrary and for example purposes.</small>

```
type: java.compile.ideconfig,
identifier: main,
{
	source_directories: [
		src/main,
		api/main,
	],
	classpath: wd:/my_lib.jar,
	source_version: 8
}
``` 

The above configuration can be created and reported using the following code:

[!embedcode](example_ideconfig/src/example/IdeConfigTaskFactory.java "language: java, range-marker-start: //snippet-start,  range-marker-end: //snippet-end, trim-line-whitespace: true")
