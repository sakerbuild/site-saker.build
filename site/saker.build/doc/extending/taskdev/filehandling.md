[!section](filedependencies.md)
[!section](fileadditiondependency.md)
[!section](customfiles.md)
[!section](filemirroring.md)

# File handling

The build system maintains its own representation of files that are available for build tasks to use. Tasks **must** use this in-memory representation of files as this is how the build system can improve its performance, reduce I/O load, employ appropriate caching, and support remote execution.

<div class="doc-warning">

**Tasks should rarely use any low level file manipulation APIs of the Java language.** Using the I/O classes in `java.io`, `java.nio.file`, `java.util.zip` and `java.util.jar` packages will often results in erroneous and inconsistent builds. Of course, exceptions may apply, but only within the services of the build system. (E.g. [](filemirroring.md))

</div>

## In-memory file system

The build system has a file system represenatation where files originate from the specified root directories. All files are accessible through these directores and tasks should use these for producing inputs and outputs to the file system. The root directories may or may not correspond to an actual file system directory on a hardware storage. This mean that a directory or files may be backed by memory, over the network, or in any other implementation dependent way.

**Important** to note that the in-memory file system has **case-sensitive** naming. If you're using Windows, it is your (the developers who use the build system) responsibility to not configure a build that can have collisions by similar case-insensitive spelling.

### Interfaces

The in-memory files are represented by the interfaces [`SakerFile`](/javadoc/saker/build/file/SakerFile.html) and [`SakerDirectory`](/javadoc/saker/build/file/SakerDirectory.html). A [`SakerFile`](/javadoc/saker/build/file/SakerFile.html) is the manifestation of a file in the in-memory file system, and is responsible for holding the contents that it is associated with. The [`SakerDirectory`](/javadoc/saker/build/file/SakerDirectory.html) represents a folder in the file system that is able to contain files in it.

The [`SakerDirectory`](/javadoc/saker/build/file/SakerDirectory.html) interface extends [`SakerFile`](/javadoc/saker/build/file/SakerFile.html). Build tasks can determine if a file is a directory, by using the `file instanceof SakerDirectory` expression.

Each file bears a name that cannot be modified during the lifetime of that object. 

### The file tree

The files are arranged in a tree like hierarchy, in which both the parents and the children have a reference to each other. Calling `getParent()` on a file will return its parent in the file system.

When a file is constructed, it has no parent and can later be added to a [`SakerDirectory`](/javadoc/saker/build/file/SakerDirectory.html). Without adding a file to a directory, it will have no effect whatsoever. A file can be only added to a parent directory *once*. After it has been added, it can be removed, but after it has been removed, it cannot be added to any directory again.

Every file has a path that represents its location from one of the root directories to the given file. Calling `getSakerPath()` will return this path.\
The result of this function may be absolute, in which case the file has a parent, and the root and each path name in the result represents the corresponding parent files. \
The result may be relative, in which case it only consists of a single path name that is the name of the file. This signals that the file currently not attached to any parents.

This file system is highly concurrent, and should be treated that way. Most of the results of methods which report state about the location of the file may be stale after they've been called, as other tasks may decide to modify the file hiearchy and therefore affect the file.

Build tasks should strive to only manipulate files that are knowingly not used by others. Tasks should only manipulate files in a unique output build directory that they've created for themselves, and place results in it in order to avoid race conditions with other tasks.

### File contents

The contents of a file may be backed in various ways defined by the respective [`SakerFile`](/javadoc/saker/build/file/SakerFile.html) implementation. The most important thing to know is that when a file is added to the file tree, the contents of it will not be automatically written to the underlying hardware. Therefore, if create a file, and add it to a directory, the contents of that file won't be persisted automatically to the associated disk location. This is where the requirement for synchronization comes in.

Synchronization of files is the process when the contents of [`SakerFile`](/javadoc/saker/build/file/SakerFile.html)s are written out to the underlying hardware disk. Synchronization can occur explicitly or implicitly. (Note: task developers shouldn't ever rely on the implicit synchronization persisting the file to a specific location. If you want to synchronize a file, call the appropriate synchronization method on it instead.)

Each file has an associated `ContentDescriptor` that is responsible for identifying the contents of a file. It is used by the build system to avoid unnecessary I/O when synchronizing files, and to detect file changes between executions.

#### Retrieving contents

The contents of a [`SakerFile`](/javadoc/saker/build/file/SakerFile.html) can be accessed in two ways. 

1. The methods `writeTo`, `openInputStream`, `openByteSource`, `getBytes`, and `getContent` can all be used to get a programatic access to the contents of the file in the appropriate representation.
	* Calling these methods will get the contents of the file using the most efficient way. They will include an implicit synchronization, unless the opening method is considered to be efficient. If the file is already synchronized with the disk, the contents from the disk will be read and provided to the caller.
		* Efficient opening methods are the ones that the given file implementations specify. An opening method is considered to be efficient if retrieving the contents consume less resources (time and space) than using the hardware disk. (E.g. a file that has its contents in a memory byte array would have all of its openings methods efficient.)
2. Calling the above methods with `Impl` appended to them. (Like `openInputStreamImpl` and others.)
	* Unlike the previous point, calling these methods will not implicitly synchronize the file, and will not contact the associated hardware disk. The efficient opening methods play no role in the execution of these methods.
	* These methods are the direct implementation of the content retrieveval functions in the underlying [`SakerFile`](/javadoc/saker/build/file/SakerFile.html)s.
	* Unless strong reasons require, developers should use the previous opening methds than these.

Generally, clients should call the content retrieval method that fits their use-case the most. For example, if you consume the contents of a file via a stream, `openInputStream` can be the best for you. If you want to hash the contents of a file, `writeTo` might be the best for you. If you need the entire contents of the file in memory, `getBytes` or `getContent` might be best.

#### Content descriptor

Each file is associated with a `ContentDescriptor` that describes the contents of a file. These descriptor objects are compared by the build system, and they are used to determine if they need synchronization, or it they've changed between executions.

Developers should implement their content descriptors in a way that defines the contents of the asociated file uniquely.

#### Synchronization

Files can be synchronized implicitly in the [previously mentioned](#retrieving-contents) ways, and explicitly via calling `synchronize` on them.

During synchronization, the content descriptor of the file is used to determine if the file has changed in relation to the disk location. If it has, then the contents of the file will be written to the given location using one of the `synchronizeImpl` methods. These methods are responsible for actually persisting the contents of a file to the given disk location.

When synchronization is called multiple times on a file, it is a no-op, if no others have modified the underlying disk location. This means that the build system will not execute additional I/O operations to overwrite files which have the same contents as expected.

## Task directories

The following directories are available for tasks to use:

* The root directories.
	* These are the directories that were configured as the root directories of the execution.
	* Available via `TaskContext.getExecutionContext().getRootDirectories()`.
* The execution working directory.
	* The working directory that was configured for the build execution.
	* Available via `TaskContext.getExecutionContext().getExecutionWorkingDirectory()`.
	* Tasks should use the task working directory instead.
* The execution build directory.
	* The base build directory that was configured for the build exection. May be `null`, meaning that no build directory was configured.
	* Available via `TaskContext.getExecutionContext().getExecutionBuildDirectory()`.
	* Tasks should use the task build directory instead.
* The task working directory.
	* This is the working directory that the tasks should use to resolve relative paths against. If a task was given a relative path as parameter, the task working directory should be considered as the base for it.
	* This directory can be configured for each started task independently.
	* Available via `TaskContext.getTaskWorkingDirectory()`.
* The task build directory.
	* This is the base directory that the task should use to put the output files in. They are also recommended to create a unique subdirectory in it to avoid race conditions with other tasks.
	* The directory may be `null`, if no build directory was configured for the build execution.
	* Available via `TaskContext.getTaskBuildDirectory()`.
	
