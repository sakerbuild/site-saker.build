# Resolve wildcard path

Wildcard paths can be used to represent a set of files during the build execution. However, sometimes you may want to deal with each matching file individually.

```sakerscript
foreach $file in std.file.wildcard(*.txt){
	print($file[Path])
}
```

The above example will simply print out the paths for each file matching the `*.txt` wildcard in the current directory.

The resolved files can be used in more complicated examples as well:

```sakerscript
saker.zip.create(
	Include: std.file.wildcard(archives/*.zip)
)
```

The above example will generate a ZIP archive that contains **all** of the contents of every other `.zip` archive in the `archives` directory.
