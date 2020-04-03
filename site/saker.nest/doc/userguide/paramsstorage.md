# Parameter storage

A parameter storage provides access to bundles that were defined using the `bundles` storage parameter.

The parameter bundle storage requires the user to specify one or more bundles to provide access to them through the storage. The specified bundles will be copied to an implementation dependent internal storage location from where they are loaded into the storage.

The following sections list the possible parameters for the parameter storage. All parameters are to be interpreted in the `<repo-id>.<storage-name>.<parameter>` format defined in the [](configuration.md) document.

## `bundles`

Parameter specifying the bundles that should be provided by the storage.

The parameter value is a semicolon (`;`) separated list of bundle paths or wildcards. The bundle paths may be resolved either against the local file system, or against the [path configuration](root:/saker.build/doc/guide/pathconfiguration.html) of the build.

If a path is prefixed by double forward slashes (`//`), then the remaining part of the path will be interpreted against the local file system. Local file system paths must be absolute.

Extra semicolons in the parameter value are ignored.

E.g.

```plaintext
lib/*.jar;///usr/lib/my-jar.jar;wd:/lib/build-lib.jar
```

The above configuration will result in using the following bundles:

* `lib/*.jar`: All the bundles in the `lib` subdirectory of the working directory that has
the `.jar` extension.
* `///usr/lib/my-jar.jar`: Will use the `/usr/lib/my-jar.jar` from the local file
system.
* `wd:/lib/build-lib.jar`: Locates the bundle with the build execution path of
`wd:/lib/build-lib.jar`. (Based on the path configuration.)

If the storage is not used in conjunction with a build execution then the path configuration depends on the
environment that sets up the repository.

*Note*: Using the `//` prefix to specify local paths may not work when using build clusters.