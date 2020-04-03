# Bundle localization

Bundle localization is the process of making the referenced bundles available on the local machine. It usually entails downloading or otherwise retrieving the given bundles and caching them in the local file system.

Using the [`nest.bundle.localize()`](/taskdoc/nest.bundle.localize.html) task can be beneficial if you don't need the bundles to be present in the build file system hierarchy, and can use them using a local file system reference.

```sakerscript
nest.bundle.localize([
	example.bundle-v1.0,
	second.bundle-v2.0
])
```

The above example will find the bundles with the specified identifiers, and localize them accordingly.

**Note** that the the task doesn't perform bundle or dependency resolution. The specified bundles must have a version qualifier. You can use the [`nest.dependency.resolve()`](/taskdoc/nest.dependency.resolve.html) task to resolve the most recent version of a given bundle.
