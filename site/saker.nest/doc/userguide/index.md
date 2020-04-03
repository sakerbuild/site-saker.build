# Repository structure

The resources in the repository is organized into packages and bundles. Each package can be only managed by one user from the community. 
The bundles in a package are published with an associated version number. After a version release has been published, it will be available for others to use, and may not be modified later.

A package release must contain at least one bundle. Different bundles in a single package are differentiated using bundle qualifiers. Qualifiers are simple names appended to the package name.

## Names

The names the saker.nest repository uses are interpreted as the following declares:

All package and bundle names are handled in a **case-insensitive** way.

The package names must be in the following format:

```plaintext
[identifier][.identifier]+
```

A package name must contain at least two identifier components separated by dot (`'.'`). Allowed characters in an identifier components are: `a-ZA-Z_0-9`. Uppercase characters are normalized to lowercase. 

Bundle names (or bundle identifiers) are based on the package names, but have additional qualifier components to them. These qualifiers are used to distinguish different bundles in an enclosing package. The bundle names have the following format:

```plaintext
[identifier][.identifier]+[-qualifier]*[-metaqualifier]*
``` 

Where the both the identifier and qualifier parts can accept the `a-ZA-Z_0-9` characters. The qualifiers can be arbitrary user defined strings, while the meta-qualifiers have a specific format. Currently the following meta-qualifiers are recognized:

* `v<version-num>`: Specifies the package version release that the bundle belongs to. E.g. if a bundle is released as part of the `1.0.1` release, then it will have the `v1.0.1` qualifier appended to it.

Examples:

```plaintext
example.package
	
	example.package-v1.0
	example.package-api-v1.0
	example.package-impl-v1.0
	
	example.package-v1.0.1
	example.package-api-v1.0.1
	example.package-impl-v1.0.1
``` 

The above lists the package `example.package` and some bundles associated with it. The bundle `example.package-v1.0` is associated with the `1.0` release of the package, and has no qualifiers. The `example.package-api-v1.0` bundle is also part of the `1.0` release, and contains some API related resources. The `impl` bundle contains implementational details. The same applies to the `1.0.1` bundles.

Note that the above example is arbitrary and for demonstrational purposes. You can use different qualifiers for your own use-case.
