# Output location

To specify the output location for the created ZIP archive, use the `Output` parameter:

```sakerscript
saker.zip.create(
	Output: path/to/output.zip,
	# ... contents ...
)
```

The created ZIP file will be in the build directory, under the `{build-dir}/saker.zip.create/path/to/output.zip`. The `Output` parameter must be a forward relative path.

If the `Output` parameter is not specified, the default is to use `output.zip` as the output path.

If you want to put the archive to a location that is not in the build directory, you can use the [`std.file.copy()`](root:/saker.standard/taskdoc/std.file.copy.html) task to copy the output.
