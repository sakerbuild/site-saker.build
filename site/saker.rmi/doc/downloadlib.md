# Download the library

You can download the library from the [saker.rmi bundle page](https://nest.saker.build/package/saker.rmi). Choose an appropriate version and click the *Download* link. Download the dependencies as well.

You can add the downloaded bundles to your project, and add it to the compilation or other classpaths as follows:

```sakerscript
saker.java.compile(
	ClassPath: [
		lib/saker.rmi-api.jar,
		lib/saker.rmi.jar,
		lib/saker.util.jar,
	]
)
```

If you don't need the RMI runtime, you can remove the `lib/saker.rmi.jar` and `lib/saker.util.jar` classpath.
