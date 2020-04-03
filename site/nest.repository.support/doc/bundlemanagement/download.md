# Bundle downloading

Bundle downloading is the process of making the referenced bundle available in the build file system hierarchy. It usually entails [localizing](localize.md) the bundles and then copying them into the build directory.

After downloading, you can retrieve the paths to the bundles and pass them to other tasks as input.

```sakerscript
nest.bundle.download([
	example.bundle-v1.0,
	second.bundle-v2.0
])
```

The above example will find the bundles with the specified identifiers, and download them accordingly.

**Note** that the the task doesn't perform bundle or dependency resolution. The specified bundles must have a version qualifier. You can use the [`nest.dependency.resolve()`](/taskdoc/nest.dependency.resolve.html) task to resolve the most recent version of a given bundle.
