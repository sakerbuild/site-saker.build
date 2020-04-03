# Local storage

A local storage provides access to bundles present at a given root storage directory.

The root storage directory location is based on the environment the repository runtime is running in or can be configured using the [`root`](#root) parameter. In case of the saker.build system, it is under the build environment storage location, and related to the class path that was used to load the repository.

Note that the local storage is not serving any caching purposes. It operates as a standalone bundle storage on the local machine. Any caching behaviour that is required by the [server storage](serverstorage.md) is done by the server storage itself.

## `root`

Parameter specifying the root storage directory for the local bundle storage.

The parameter value is an absolute path on the local file system that specifies the root directory that the local storate should use to store and retrieve bundles.

The default value is in an implementation dependent subdirectory of the repository storage directory. (The repository storage directory location is provided by the enclosing environment that loaded the repository.)
