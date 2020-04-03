# Localize bundles

See also: [](../bundlemanagement/localize.md)

Localizing bundles is the process of making them available on the local file system. The [`nest.bundle.localize()`](/taskdoc/nest.bundle.localize.html) task performs this operation:

```sakerscript
nest.bundle.localize([
	example.bundle-v1.0,
	second.bundle-v2.0
])
```

The above example will find the bundles with the specified identifiers, and localize them accordingly.

You can also specify the output of task resolution as the input for the task:

```sakerscript
nest.bundle.localize(nest.dependency.resolve([
	example.bundle
]))
```

The above will resolve the dependencies of `example.bundle`, and localize all dependent bundles (including `example.bundle`).
