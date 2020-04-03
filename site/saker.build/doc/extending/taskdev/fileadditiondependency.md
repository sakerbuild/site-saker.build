# File addition dependency

In the [previous article](filedependencies.md) we've seen that when the file dependencies are reported appropriately, the task will be rerun if the reported files change. However, tasks also need to be able to specify a dependency in which case adding a file will cause the reinvocation of the task.

This requirement is what file addition dependencies satisfy. They use a file collection strategy to dynamically search for the files, and they can be reported by tasks to check if the found files is the same as in previous run. When the collection strategy finds a new file, the task will be rerun with a delta that signals the new file addition.

The following example task takes a wildcard parameter and will print out the paths of all files that match that given wildcard:

[!embedcode](example_fileadddep/src/example/PrintFileNamesTaskFactory.java "language: java, range-marker-start: //snippet-start,  range-marker-end: //snippet-end, trim-line-whitespace: true")

If we call it the following way:

```sakerscript
example.print.files(Files: **/*.txt)
```

That will print out the absolute paths of every file that is in the working directory tree and has the extension `.txt`. If any new file is added that is matched by the wildcard, the task will rerun accordingly.

The interface [`FileCollectionStrategy`](/javadoc/saker/build/task/dependencies/FileCollectionStrategy.html) is responsible for providing the implementation for collecting the interested files. It consists of a `collectFiles` method that collects the files based on the directories of the task context and build execution. That method is also called by the build system in order to determine changes between executions.

In the above example, we create a new instance with the built-in [`WildcardFileCollectionStrategy`](/javadoc/saker/build/task/utils/dependencies/WildcardFileCollectionStrategy.html) that supports collecting files using a wildcard. We use the task utilities to collect all the files that the given strategy (in our case the wildcard) locates. The `collectFilesReportInputFileAndAdditionDependency` method will also report the dependencies for file additon, and the found input files as well.

The tasks are not required to use the task utilities for reporting such dependencies, but are recommended to do so. They can use the `TaskContext.reportInputFileAdditionDependency` method as well.

**Important:** If a file addition dependency is reported to the build system, the files it finds need to also be reported as input dependencies. The build system discovers new file additions by collecting the files using the given file collection strategy, and cross-examines the results with the reported input file dependencies. If a discovered file was not reported as an input dependency previously with the same tag, an input file addition delta will be triggered for that file.

The file tags for file addition dependencies work the same way as with [file dependencies](filedependencies.md#dependency-tags). The previously mentioned cross-examination of dependencies will not escape tag boundaries.

## File collection strategies

The `FileCollectionStrategy` interface play the core role when determining file additional changes. The `collectFiles` function is responsible for locating the interested files in an implementation dependent manner. The function receives the execution-wise and task-wise directories, and is required to locate the interested files.

File collection strategies are required to adhere to the `hashCode`, `equals` contract specified by the `Object` class. If two strategies find the same files given the same argument directories, they are considered to be equal.

The file collection strategies are also strongly recommended to implement some serialization functionality, preferably implementing `Externalizable` to work properly between executions.

Some example implementations of [`FileCollectionStrategy`](https://github.com/sakerbuild/saker.build/blob/master/core/common/saker/build/task/dependencies/FileCollectionStrategy.java) can be seen in the build system source code:

* [`WildcardFileCollectionStrategy`](https://github.com/sakerbuild/saker.build/blob/master/core/common/saker/build/task/utils/dependencies/WildcardFileCollectionStrategy.java)
* [`RecursiveIgnoreCaseExtensionFileCollectionStrategy`](https://github.com/sakerbuild/saker.build/blob/master/core/common/saker/build/task/utils/dependencies/RecursiveIgnoreCaseExtensionFileCollectionStrategy.java)
* [`PathFileCollectionStrategy`](https://github.com/sakerbuild/saker.build/blob/master/core/common/saker/build/task/utils/dependencies/PathFileCollectionStrategy.java)
* [`DirectoryChildrenFileCollectionStrategy`](https://github.com/sakerbuild/saker.build/blob/master/core/common/saker/build/task/utils/dependencies/DirectoryChildrenFileCollectionStrategy.java)

