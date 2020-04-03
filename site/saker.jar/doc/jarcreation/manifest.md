# Manifest

As a base about the manifest of a JAR file we recommend reading the manifest part of the [JAR File Secification](https://docs.oracle.com/javase/8/docs/technotes/guides/jar/jar.html#JAR_Manifest).

The manifest of a JAR file is the entry with the path `META-INF/MANIFEST.MF`. The [`saker.jar.create()`](/taskdoc/saker.jar.create.html) tasks supports specifying the manifest by direct resource inclusion and via task parameters:

```sakerscript
saker.jar.create(
	Manifest: {
		MainAttributes: {
			Main-Class: example.Main
		}
	}
)
```

In the above example we specify the `Main-Class` attribute to be set in the manifest of the created JAR file to have the value `example.Main`. The manifest file will have the following contents in the created archive:

```plaintext
Manifest-Version: 1.0
Main-Class: example.Main
```

Note that the `Manifest-Version: 1.0` entry is automatically included, as it is required by the manifest specification.

You can also specify per-entry attributes:

```sakerscript
saker.jar.create(
	Manifest: {
		MainAttributes: {
			Main-Class: example.Main
		},
		EntryAttributes: {
			FirstEntry: {
				Attr: 123,
			},
			SecondEntry: {
				Attr: 456,
			}
		}
	}
)
```

The above parameters specify the following manifest contents:

```plaintext
Manifest-Version: 1.0
Main-Class: example.Main

Name: SecondEntry
Attr: 456

Name: FirstEntry
Attr: 123
```

Note that if you already include a manifest file using the `Resources` or `Includes` task parameters, the attributes specified in the `Manifest` parameter will overwrite already existing entries. The existing manifest and the attributes specified in the parameter are merged.
