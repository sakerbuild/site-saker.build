# Put file

The [`std.file.put()`](/taskdoc/std.file.put.html) task can be used when you want to write some string contents to a given file. It simply takes the target file location and file contents as its input, and writes it to the given location:

```sakerscript
std.file.put(
	std.file.local("/home/user/file.txt"),
	Contents: "example_content"
)
``` 

The above will result in a file created at the local file system path `/home/user/file.txt` with the contents `example_content`. The task can be used to put files to local and execution file locations. The `Charset` parameter can be used to specify the encoding of the bytes when the characters are written.

It is generally not recommended to use this task to write files in the build execution hierarchy, only to write files that are the final output of the build. If you write a file that is an input to some other tasks, you need to ensure proper execution order of those tasks.