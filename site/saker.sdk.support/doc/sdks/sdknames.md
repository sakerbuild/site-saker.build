# SDK names and identifiers

When working with SDKs, you will need to give a name for them. The SDK names may be arbitrarily specified, however, when referencing paths and properties using [`sdk.path()`](/taskdoc/sdk.path.html) and [`sdk.property()`](/taskdoc/sdk.property.html), then the same names must be used for the associations to work.

**Important** is that the SDK names are interpreted in a case-insensitive way. The following tasks achieve the same and all return a path reference for the SDK named `java`:

```sakerscript
sdk.path(Java, Identifier: exe.java)
sdk.path(java, Identifier: exe.java)
sdk.path(JAVA, Identifier: exe.java)
```

**Note** that the path and property identifiers may or may not be handled in a case-insensitive way. It depends on the SDK that they're associated with. In general, you should treat identifiers that they are case-sensitive.

Some SDK names may be handled specially by the task that interprets them. For example the `saker.java.compile()` task will decide the compilation JDK based on the specified `Java` SDK:

```sakerscript
saker.java.compile(
	SourceDirectories: src,
	SDKs: {
		Java: saker.java.sdk(13)
	}
)
```

The above example will use a JDK with a major version of 13 to compile the sources in the `src` directory. 
