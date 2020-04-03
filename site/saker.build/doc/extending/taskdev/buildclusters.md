# Build clusters

Build clusters allow tasks to be dispatched to execute their works on remote worker machines. This can increase build performance as it can utilize significantly more resources than a single build computer.

Adding build cluster support for a task implementation is not straightforward, error-prone, and takes a lot of care for object management. One needs to keep in mind that the tasks running on a cluster needs to communicate with the build coordinator (the main machine that runs the build) and it can introduce a lot of implicit communication over the network that may result in degrading performance.

In order to allow your task to be considered for build cluster use, you need to declare the associated capability in your [`TaskFactory`](/javadoc/saker/build/task/TaskFactory.html) implementation. (See [](taskcapabilities.md#capabilities).)

## Cluster architecture

When the build system is configured to use clusters, it will manage one or more remote workers which can be used to dispatch task executions to it. The main machine which executes the build is called the coordinator, and the workers are called clusters.

The environment ([`SakerEnvironment`](/javadoc/saker/build/runtime/environment/SakerEnvironment.html) instance) of the coordinator and clusters may be configured differently. This means that it is possible for the coordinator and a cluster to run on different operating systems, have different installed software on it, and may be completely different in other aspects too. The machines that take part in the build executions are not required to have the same properties.

During a build with clusters, the execution context ([`ExecutionContext`](/javadoc/saker/build/runtime/execution/ExecutionContext.html) instance) is shared by all tasks that is being executed. Even if your task is dispatched to a remote cluster, the execution context will still be the same and is not affected by the cluster configuration.

The task context ([`TaskContext`](/javadoc/saker/build/task/TaskContext.html) instance) is private to the task and is not affected by the location where the task is being executed.

### Communication

The communication between the coordinator and the clusters is done using the [saker.rmi](root:/saker.rmi/index.html) (Remote Method Invocation) library. It is able to transparently forward method calls over the network and manage the transferring of the objects. It allows a wide range of configuration options for clients to specify how a given object is transferred over the network.

Task implementers should be familiar with the saker.rmi library, and carefully inspect the called methods of the build system APIs when build clusters may be invoked. In most cases, calling remote methods will work the same way as running on the coordinator machine, however, there are some cases the developer needs to pay special attention.

Another important aspect of this, is that calling a lot of remote methods may result in a significant amount if network communication. This is rarely beneficial, as the network communication may take comparable amount of time in relation to the time it takes to actually run the method. Developers should strive to avoid calling possibly remote methods in loops, and otherwise should attempt to do operations using a [single call](#network-communication) rather than multiple.

### File handling

The build system implements its own [file handling](filehandling.md) via the [`SakerFile`](/javadoc/saker/build/file/SakerFile.html) and related interfaces. All of the mentioned file hierarchy is stored on the coordinator machine. That is, if you want to add a file to the build system representation, you need to transfer the created file to the coordinator machine via RMI.

This is required to provide a more robust build execution, as if the files weren't transferred to the coordinator, then in case the cluster connection is lost, the build would fail as the files would be no longer accessible. A second reason is that without transferring the files, there would be an implicit network request every time the files are accessed, and unexpectedly hinder performance. The third reason is that the [`SakerFile`](/javadoc/saker/build/file/SakerFile.html) hierarchy requires the files to be a specific class, and it is not possible to represent it correctly via RMI.

### Content caching

It is a common scenario to access some files repeatedly from given clusters. This could have a significant performance cost if every time the file contents are accessed, the coordinator machine would be contacted even though the contents are unchanged. In order to avoid the possible performance drop, the build system employs caching of file contents on the clusters.

This mechanism means that every time a file ([`SakerFile`](/javadoc/saker/build/file/SakerFile.html)) is opened with possible implicit synchronization, the file may be opened from a previously persisted location on the cluster instead of over the network. This also implies that task developers should *not* depend on the implicit synchronization persisting the file contents to the appropriate locations.  

# Task implementation

The following sections will describe what you should take into account when implementing tasks that use the build clusters.

## Output files

As the output of your task, you probably want to create files that hold the results of it. The [`SakerFile`](/javadoc/saker/build/file/SakerFile.html) API can be used to manage the files accessible by the build system. As [previously mentioned](#file-handling), the files reside on the coordinator machine, therefore any created files need to be transferred over the network.

This behaviour may require your code to be adjusted for it to work properly. The following will work for tasks which execute on clusters, and on tasks that does not as well.\
The files need to be transferred in a non-remote way to the coordinator machine, that is, the actual [`SakerFile`](/javadoc/saker/build/file/SakerFile.html) instance that is added to the parent [`SakerDirectory`](/javadoc/saker/build/file/SakerDirectory.html) will not be the same instance. This means that some method calls on the added file instance may not represent the actual state of the file. The following example clarifies this:

```java
SakerDirectory outputdir;
SakerFile myfile; // = ...
outputdir.add(myfile);

SakerPath filepath = myfile.getSakerPath();
// here filepath doesn't contain the actual path of the file
```

In the above example we add our custom output file to the given output directory. Calling [`getSakerPath()`](/javadoc/saker/build/file/SakerFile.html#getSakerPath--) on the added file may not be actually return the absolute path of the added file when running on a build cluster. Note that if you don't use clusters, this will work properly nonetheless.

This is caused by the file being transferred to the coordinator machine, and a different instance is being added to it instead of our instance. As a first solution, we recommend not to use the files after being added to a directory. If you want to do so, you can accomodate this behaviour by changing your code to use the task execution utilities of the task context:

```java
TaskContext taskcontext;
SakerDirectory outputdir;
SakerFile myfile;
// note the reassignment
myfile = taskcontext.getTaskUtilities().addFile(outputdir, myfile);

SakerPath filepath = myfile.getSakerPath();
// here the filepath will contain the valid and expected path
```

The [`addFile`](/javadoc/saker/build/task/TaskExecutionUtilities.html#addFile-saker.build.file.SakerDirectory-saker.build.file.SakerFile-) method of the task utilities will return the file that is actually being added to the given directory. The [task utilities](/javadoc/saker/build/task/TaskExecutionUtilities.html) contain other methods to support file handling for remote tasks, we recommend using them accordingly.

## File transfer

The created [`SakerFile`](/javadoc/saker/build/file/SakerFile.html) instances need to be appropriately transferred to the coordinator machine. This is supported by overriding the [`SakerFile.getRemoteExecutionRMIWrapper()`](/javadoc/saker/build/file/SakerFile.html#getRemoteExecutionRMIWrapper--) method in your file implementation. When calling build system API methods, the specified wrapper is used to transfer the file appropriately.

You must not define a custom RMI transfer mechanism for your file class, but instead override this method to specify it. Defining a custom transfer mechanism will result in errors, as the requested files may be two-way transferred when using remote execution. This is erroneous behaviour, as files are stateful objects, and they will lose their state information during transfer.

## Network communication

Task developers should seriously consider the effect of implicit network calls when some interface methods of the build system API is called. When a task is being remotely executed, all task context, execution context, file management, and other methods may result in an implicit network request.

Calling these methods are usually fine, but calling them in a loop may incure a performance penalty. We created the [task execution utilities](/javadoc/saker/build/task/TaskExecutionUtilities.html) in order to solve this problem which contains bulk methods for interacting with the task context. One example of using it is the following:

```java
TaskContext taskcontext;
for (SakerFile file : files) {
	taskcontext.reportInputFileDependency(null, 
		file.getSakerPath(), file.getContentDescriptor());
}
```

The above loop can cause many network requests which can make your task execute longer. Instead of doing toe above, use the task execution utilities:

```java
TaskContext taskcontext;
taskcontext.getTaskUtilities().reportInputFileDependency(null, files);
```

They are semantically the same, but only needs one network request to report the dependencies. The above codes work the same regardless of the execution location, be it the coordinator or a cluster.

There are other methods in the utilities interface which you can use to reduce your task context method calls, and we recommend doing so. As the build system evolves, additional methods may be added to the utilities to support closer integration.

## Scalability

When developing for cluster use, make sure you examine the possible task executions for small and large inputs as well. We recommend that you handle appropriately if the task is being run with small input sizes, as in that case using clusters may not actually provide a performance benefit.

We recommend making your task able to be configured explictly for allowing and disallowing cluster execution, and otherwise use heuristics to determine if it is feasible to use clusters. (Like number of input files, algorithmical complexity, etc...)  

## Environment selection

The clusters may have completely different configuration on their nodes. They may use different operating systems, have different tooling installed, and they may differ in various other ways. Your task needs to choose a suitable execution cluster to run on. 

The [task environment selector](taskcapabilities.md#execution-environment-selection) can be used to implement this functionality which is responsible for selecting the environment to execute the task on based on various [environment properties](propertydependencies.md).
