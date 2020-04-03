# Include archive

Including the contents of an archive into the created archive works very similarly as [adding directory contents](adddirectory.md), however, you specify an archive instead of a directory as the root:

```sakerscript
saker.zip.create(
	Include: [
		everything.zip,
		{
			Archive: images.zip,
			Resources: **/*.png,
			TargetDirectory: img
		}
	]
)
```

Let's assume that the referenced ZIP files have the following contents:

`everything.zip`:
```plaintext
file.txt
dir/dirfile.txt
```

`images.zip`:

```plaintext
favicon.ico
icon.png
bg/background.png
```

If we call the above task, the contents of the archive will be the following:

```plaintext
file.txt
dir/dirfile.txt
img/icon.png
img/bg/background.png
```

The first simple inclusion declaration will include all files from the `everything.zip` archive. They are placed into the resulting archive with the same path as they are present in `everything.zip`.

The second inclusion declaration only includes the entries that end with `.png`. They are placed in the `img` directory of the created archive. If the `TargetDirectory` is not specified, then the same archive path will be used as in the originating archive.
