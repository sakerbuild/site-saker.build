# Filter sources

When specifying a source directory as an input, you can specify which source files should be compiled under it. The source files can be specified using wildcard patterns which are resolved with the associated directory:

```sakerscript
saker.java.compile({
	Directory: src,
	Files: **/*Test.java
})
```

The above will only compile source files that end with `Test.java`, and are under the `src` directory. You can specify multiple wildcards for the `Files` property:

```sakerscript
saker.java.compile({
	Directory: src,
	Files: [
		**/*Test.java,
		**/Foo*.java
	]
})
```

In this case any source file that starts with `Foo` will be compiled additionally to the previous example.

Generally you wouldn't need to use source file filters, as common Java development practices don't really make use of these. However, you have the option nonetheless.
