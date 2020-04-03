# Local file output

The [`std.file.copy()`](/taskdoc/std.file.copy.html) task can be used to copy files to a specified local file system path as the result of the build execution.

```sakerscript
$zip = saker.zip.create(### ... ###)
std.file.copy(
	Source: $zip[Path], 
	Target: std.file.local("/home/User/output.zip")
)
```

The above simplified script will create a new ZIP archive as part of the build, and will copy it to the `/home/User/output.zip` path on the local file system.
