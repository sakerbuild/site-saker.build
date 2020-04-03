# Add directory tree

You can add multiple files that are present in a directory the following way:

```sakerscript
saker.zip.create(
	Resources: [
		{
			Directory: res,
			Resources: **
		},
		{
			Directory: images,
			Resources: **/*.png,
			TargetDirectory: img
		}
	]
)
```

Let's assume that we have the following hierarchy in the *working directory* of the task:

```plaintext
readme.txt
res/file.txt
res/dir/dirfile.txt
images/favicon.ico
images/icon.png
images/bg/background.png
```

If we call the above task, the contents of the archive will be the following:

```plaintext
file.txt
dir/dirfile.txt
img/icon.png
img/bg/background.png
```

The first resource declaration will include all files from the `res` directory that matches the `**` wildcard. This means all the files in the subtree of the directory.

The second resource declaration will include the files that have the `.png` ending in the `images` directory subtree. It will put the files in the `img` directory in the archive.
