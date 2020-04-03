# ZIP contents

To specify the archive contents you have two possibilities:

* Specify the files that should part of the archive. (`Resources` parameter)
* Include entries from other ZIP archives. (`Include` parameter)

## `Resources`

The `Resources` parameter of the [`saker.zip.create()`](/taskdoc/saker.zip.create.html) task can be used to specify files to be directly added to the archive, or to add files under a given subdirectory.

In both cases, you can control the archive location of the added files using the `TargetDirectory` property.

All of the files which are added using the `Resources` parameter will have the same modification time set for them, which equals to the `ModificationTime` parameter. (Or the epoch 1970-01-01T00:00:00Z if unspecified.)

### Adding files

When adding files to the archive, the files will be placed in the root of the created archive with the same name as the added file. If the `TargetDirectory` property is specified, they will be placed under that directory path.

```sakerscript
saker.zip.create(
	Resources: [
		input.txt,
		{
			Files: [
				libfoo.so,
				libbar.so
			],
			TargetDirectory: lib
		}
	]
)
```

In the above example, the created ZIP archive will have the following files in it:

```plaintext
input.txt
lib/libfoo.so
lib/libbar.so
```

### Adding directory contents

You can specify a directory and its subtree to be added to the created archive. This is a common use-case, as developers often store their to-be-archived resources in a single folder for easier management. To add a directory and its contents to the archive, use the following:

```sakerscript
saker.zip.create(
	Resources: {
		Directory: res,
		Resources: **
	}
)
```

The `Directory` property specifies the path to the directory that should be added to the archive, while the `Resources` property specifies a wildcard pattern that selects the files to be added. The created archive will contain all files that is in the subtree of the `res` directory. The archive paths of the files are relativized against the `res` directory path.

You can also specify the `TargetDirectory` property alongside the `Directory` and `Resources`, that will cause the files to be placed under the specified directory path.

## `Include`

The `Include` parameter of the [`saker.zip.create()`](/taskdoc/saker.zip.create.html) tasks allows you to include entries from other ZIP archives. You can specify the resources to be included using wildcard patterns, and can set a target directory under which they should be placed.

```sakerscript
saker.zip.create(
	Include: [
		resources.zip,
		{
			Archive: input.zip,
			Resources: **/*.txt,
			TargetDirectory: input_res
		}
	]
)
```

The above example will include *all* entries from the `resources.zip` in the created archive, and will include all `txt` files from the `input.zip` archive. The `txt` files will be placed under the `input_res` path for the created archive. The `input_res` target directory is prepended to the entry paths, the `txt` files are placed with respect to their original archive paths.

The modification time of the included entries are *preserved*. The `ModificationTime` parameter of the task doesn't override the modification time from the included entries.

