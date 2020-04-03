# Download bundles

See also: [](../bundlemanagement/download.md)

Downloading bundles is the process of making them present in the build execution file hierarchy. The [`nest.bundle.download()`](/taskdoc/nest.bundle.download.html) task performs this operation:

```sakerscript
nest.bundle.download([
	example.bundle-v1.0,
	second.bundle-v2.0
])
```

The above example will find the bundles with the specified identifiers, and download them accordingly.

You can also specify the output of task resolution as the input for the task:

```sakerscript
nest.bundle.download(nest.dependency.resolve([
	example.bundle
]))
```

The above will resolve the dependencies of `example.bundle`, and download all dependent bundles (including `example.bundle`).
