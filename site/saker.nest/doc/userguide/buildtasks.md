# Build tasks

Bundles uploaded to the repository may contain build tasks that can be invoked using the saker.build system during build execution. The build tasks can be invoked in the build script by simply referencing it by name and [bundle qualifiers](index.md#names). Task qualifiers work the same way as bundle qualifiers and are used to choose the bundle to load the task from.

Example, if the `example.package` publishes the `example.task` build task:

```sakerscript
# invokes the task from the latest example.package release
example.task()
# invokes the task from the v1.0 release
example.task-v1.0()
# invokes the task from the latest release
# and from the example.package-q1 bundle
example.task-q1()
# invokes the task from the v1.0.1 release
# and from the example.package-q1 bundle
example.task-q1-v1.0.1()
```

Using task qualifiers, you can choose which task implentation to use during the build execution. Generally, we recommend to include your tasks in your main bundle (that is the one without any extra qualifiers), so there are no confusion about what kind of qualifiers should be used for a given task invocation. However, this method supports use-cases where different task implementations should be chosen based on the circumstances.

If no version is specified for a given task invocation, the repository will choose the latest release version that is available.