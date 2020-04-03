# As bundle dependency

If you're developing a saker.nest bundle, then you can add it to the runtime dependency of your bundle. See the [dependency file format](root:/saker.nest/doc/devguide/bundleformat.html#dependency-file) for declaring bundle dependencies. A simple example:

```plaintext
saker.rmi
	classpath: [0)
```

The above will always use the most recent version of saker.rmi. If you don't want to use the runtime of the RMI library, only the API, you can depend on `saker.rmi-api` instead.

**Note** that however, as the saker.rmi is distributed as part of the saker.build system as well, you most likely won't need to add the saker.rmi library directly to your bundle, as the classes are already available from the build system under the `saker.build.thirdparty.saker.rmi` Java package name.
