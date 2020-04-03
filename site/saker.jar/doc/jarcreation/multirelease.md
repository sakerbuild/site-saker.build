# Multi-release contents

The [`saker.jar.create()`](/taskdoc/saker.jar.create.html) task allows specifying contents for the archive that are automatically placed to the appropriate subdirectory location for [multi-release JAR files](https://openjdk.java.net/jeps/238).

The contents for multi-release resources can be specified the same way as the normal contents are specified, but are bound to a given JRE major version:

```sakerscript
saker.jar.create(
	Resources: readme.txt,
	MultiReleaseContents: {
		8: {
			Resources: v8/versioned.txt,
		},
		9: {
			Resources: v9/versioned.txt,
		},
	},
)
```

The above parameter values specify the JAR contents to be as follows:

```plaintext
readme.txt
versioned.txt
META-INF/versions/9/versioned.txt
```

The `MultiReleaseContents` map parameter takes the integral major version numbers as the keys and the content specifications as values. The values may include the `Resources` and `Includes` properties the same way they are used as the task parameters. Any contents that are specified in the `MultiReleaseContents` value are automatically prepended with the appropriate versioned path. (E.g. `META-INF/versions/9/`)

For multi-release contents that are specified for major version 8 or lower, no paths are prepended, since the multi-release feature was introduced in major version 9.

**Important**: Specifying multi-release contents will cause the `Multi-Release: true` entry to be added to the manifest file of the archive.
