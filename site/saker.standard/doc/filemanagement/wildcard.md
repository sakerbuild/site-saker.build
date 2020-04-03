# Wildcards

The [`std.file.wildcard()`](/taskdoc/std.file.wildcard.html) and [`std.file.wildcard.local()`](/taskdoc/std.file.wildcard.local.html) tasks allow you the resolve a [wildcard path](root:/saker.build/doc/guide/wildcards.html) and query all files that matches it.

The output of these tasks is a set of file references that can be passed to other tasks which support them as inputs.

As an example, we print out the paths of files in the working directory that end with `*.txt`:

```sakerscript
foreach $file in std.file.wildcard(*.txt){
	print($file[Path])
}
```

If the contents of the working directory is the following:

```plaintext
file.txt
readme.txt
```

The the output will be:

```plaintext
wd:/file.txt
wd:/readme.txt
```

(Given that the working directory is `wd:/`.)

## Local wildcard

The [`std.file.wildcard.local()`](/taskdoc/std.file.wildcard.local.html) task allows you to query the local file system using a wildcard. Following up on the previous example:

```sakerscript
foreach $file in std.file.wildcard.local(
	*.txt, 
	Directory: "c:/path/to/project/directory"
) {
	print($file[LocalPath])
}
```

The the output will be:

```plaintext
c:/path/to/project/directory/file.txt
c:/path/to/project/directory/readme.txt
```

Note that you don't always need to specify the `Directory` parameter for the local wildcard task. You can use absolute wildcards:

```sakerscript
foreach $file in std.file.wildcard.local(
	"c:/path/to/project/directory/*.txt"
) {
	print($file[LocalPath])
}
```

And the output will be the same.