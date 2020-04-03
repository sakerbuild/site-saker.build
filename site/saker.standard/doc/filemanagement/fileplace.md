# File place

The [`std.file.place()`](/taskdoc/std.file.place.html) task allows you to retrieve a path where you can work in an use-case dependent manner during the build execution.

The task returns a path under the build directory, where you can place, modify, and work with files in general. The directory can be used to perform simple file operations that can be used later as an input to other tasks.

```sakerscript
std.file.place(custom/workplace)
```

The above will return a path that points to the `{build-dir}/std.file.place/custom/workplace` path.

Generally, the task has limited use-case, however, if you want a file system location where you can work without worrying about interfering with other task results, the task may be suitable.
