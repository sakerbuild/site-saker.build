# As bundle dependency

If you're developing a saker.nest bundle, then you can add it to the runtime dependency of your bundle. See the [dependency file format](root:/saker.nest/doc/devguide/bundleformat.html#dependency-file) for declaring bundle dependencies. A simple example:

```plaintext
saker.util
	classpath: [0)
```

The above will always use the most recent version of saker.util.

**Note** that however, as the saker.util is distributed as part of the saker.build system as well, you most likely won't need to add the saker.util library directly to your bundle, as the classes are already available from the build system under the `saker.build.thirdparty.saker.util` Java package name.
