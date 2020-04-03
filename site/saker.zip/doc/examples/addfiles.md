# Add files

For adding files to the create archive, use the `Resources` parameter of the [`saker.zip.create()`](/taskdoc/saker.zip.create.html) task:

```sakerscript
saker.zip.create(
	Resources: [
		readme.txt,
		res/file.txt,
		img/icon.png
	]
)
```

The created ZIP archive will have the following contents:

```plaintext
readme.txt
file.txt
icon.png
```

**Note** that any files that you specify this way will be added to the root of the archive. If the files to be included reside in a directory, that is not taken into account.

If you want to put the files to a specific directory, use the following:

```sakerscript
saker.zip.create(
	Resources: [
		readme.txt,
		{
			Files: res/file.txt,
			TargetDirectory: resources
		},
		{
			Files: img/icon.png,
			TargetDirectory: img
		}
	]
)
```

The above will create a ZIP archive that contains the files as follows:

```plaintext
readme.txt
resources/file.txt
img/icon.png
```

If you want to take the path to the included resource into account for the archive path, add them in relation with a given directory. See [](adddirectory.md).