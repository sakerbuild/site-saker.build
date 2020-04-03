# File mirroring

File mirroring is the process of taking a file in the build execution file hierarchy and making them available on the local file system. This can be useful when files need to be passed as inputs to other external processes, or in other use-cases.

The [`std.file.mirror()`](/taskdoc/std.file.mirror.html) task allows you to perform this operation:

```sakerscript
$localpath = std.file.mirror(path/to/file.txt)
$localdirpath = std.file.mirror(path/to/directory)
```

The above will mirror the files with the specified execution paths. The result of the task is the path on the local file system.

You may want to convert it to a local file reference using the [`std.file.local()`](/taskdoc/std.file.local.html) task.

**Note** that the local file system paths and execution file paths are **not** interchangeable. If a task expects file paths, it is most likely an execution path to a file, unless explicitly specified otherwise.

The [`std.file.mirror.path()`](/taskdoc/std.file.mirror.path.html) task can be used if you want to retrieve the mirror path that corresponds to a given path, but doesn't execute the mirroring itself.