# Copying files

As a part of the build execution you may want to copy some files from a location to a different one. The [`std.file.copy()`](/taskdoc/std.file.copy.html) task allows performing file copy operations.

It takes file locations as its input, and executes the copy based on them. The inputs may be execution paths or local file references. The tasks support copying single files as well as directories and its contents.

A simple example for copying two files:

```sakerscript
std.file.copy(
	Source: file.txt,
	Target: resources/contents.txt
)
```

The above will copy the `file.txt` from the working directory to the `resources/contents.txt` path. The file is renamed as a result of the copying.

The task supports copying contents of directories. If the `Source` is a directory then the subtree of that directory will be copied if the files match the specified `Wildcards`:

```sakerscript
std.file.copy(
	Source: images,
	Target: resources,
	Wildcards: **/*.png
)
```

The above will copy the `images` directory to the `resources` directory, and all `png` files in its subtree.

Note that file copying should be used only when necessary. Generally it is not recommended to copy files to a target that is not under the build directory, as modifying the user files is discouraged.

An use-case for copying is that the generated files during the build process is copied to an unrelated location on the local file system that can be shared with other projects:

```sakerscript
$jar = saker.jar.create(### ... ###)
std.file.copy(
	$jar[Path],
	Target: std.file.local("/home/user/other_project/lib/mylib.jar")
)
```

The above example will create a JAR file as a result of the build process, and then copy that to the library directory of a different project. This can avoid the necessity of manually copying the result of the build when it is also used by other endeavors.
