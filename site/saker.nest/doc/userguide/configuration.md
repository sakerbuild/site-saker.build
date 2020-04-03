[!section](serverstorage.md)
[!section](localstorage.md)
[!section](paramsstorage.md)
[!section](constraints.md)

# Runtime configuration

When used, the repository runtime can be configured using string key-value parameters. If used with saker.build, the build execution user parameters (`-U`) command line option can be used to configure the repository.

The runtime will interpret these parameters and configure itself and its storages based on them.

The format of the parameter names are the following:

```plaintext
<repo-id>.<storage-name>.<parameter>
```

Where `repo-id` is the repository identifier assigned for the loaded repository, `storage-name` is the name of the [storage](#repository-storages) being configured, and `parameter` is the name of the parameter associated with the given storage.

One can note that this naming scheme allows multiple saker.nest repositories to be loaded in a single build execution. If they have different repository identifiers, they can be configured independently using the user parameters.

The commonly used (and default) repository identifier for saker.nest is `nest`.

## Repository configuration

A saker.nest repository configuration consists of different storage configurations, and general repository runtime configurations. Each configured *storage* is used to load and look up bundles and tasks during the execution.

If the `storage-name` part of a parameter equals to `repository`, then the parameter will apply to the overall repository configuration instead of the storage with the given name.

### Repository parameters

The following parameters can be used to configure the the overall repository runtime.

#### `storage.configuration`

The parameter specifies the storages that should be used during the operation with the repository. It defines how the repository will find, look up, and load bundles.

The value of this parameter contains the configured storage names and their types. They can be specified in a `<name>:<type>` format where the name must only contains alphabetic (`a-z`, `A-Z`), numberic (`0-9`), or underscore (`_`) characters. The type must be one of `server`, `local`, or `params`.

Multiple storage declarations can be specified in a list that is enclosed in brackets (`[`, `]`) and separated by commas (`,`). The list declarations can be nested. These subsequent storage declarations define the lookup behaviour assigned to each configuration.

The declared storages can be configured with parameters that begin with the repository identifier part that is the same as this parameter, and continues with the name of the storage and ends with the parameter name in a dot separated format. Parameters for bundle storages must have the following format:

```plaintext
<repo-id>.<storage-name>.<param>
```

Examples:

```plaintext
"server: server"
```

The only storage configuration that is used is a [server storage](serverstorage.md). The configuration will only use bundles that is available from that server.

If the storage name is the same as the storage type, the storage name can be omitted. Specifying simply `":server"` is the same as the above declaration.

---

```plaintext
"[:params, :local, :server]"
```

The above is the **default storage** configuration if the parameter is not specified. It contains 3 storages:

* `params`: A [parameter storage](paramsstorage.md). Bundles from `local` and `server` is visible from it.
* `local`: A [local storage](localstorage.md). Bundles from `server` is visible to it, but bundles from `params` are not.
* `server`: A [server storage](serverstorage.md). It can only use bundles that it contains.

The definiton place of storages affect what bundles they can use. This specifically important when dependencies are resolved in the repository. If a bundle from `params` depend on a bundle from `local`, the dependency can be successfully resolved. However, if a bundle in `local` tries to depend on a bundle from `params`, the dependency resolution will fail.

---

```plaintext
"[p1:params, p2:params]"
```

Multiple storages with the same type can be declared. In this case there are 2 [parameter storages](paramsstorage.md) in the specified order. The parameters `<repo-id>.p1.bundles` and `<repo-id>.p2.bundles` can be used to differently configure the two storages.

---

```plaintext
"[[p3:params, :local], p4:params]"
```

In the above example we use nested scopes to specify different lookup properties. In this case the bundles from `local` will be visible to `p3`. The bundles from `p4` will not be visible to either `p3` or `local`, as they've been declared in a different scope.

If we would want to make the bundles in `p4` visible to `p3`, but still keep them from `local`, we can use the following configuration:

```plaintext
"[p3:params, [:local], p4:params]"
```

In this case the bundles from `local` couldn't depend on `p4`, but the bundles in `p3` could use the bundles from `p4`.

---

The bundle storages can be repeated in order to make them accessible through multiple lookup scopes. However, when doing so, all declarations must have the same tail resolution. (That is, all repeating configurations must have the same lookup scope after them.) See the following:

```plaintext
"[[p5:params, :local], [p6:params, :local]]"
```

In this case both `p5` and `p6` have access to the bundles from `local`, while neither `p5` can depend on bundles from `p6`, and the `local` storage doesn't see bundles in `p6` as well.

Note that modifying the above configuration the following way will cause an initialization error:

```plaintext
"[[p5:params, :local], [p6:params, :local, :server]]"
```

This is due to the `local` storage has different tail resolution defined in the locations it appears. To have the `server` storage visible for `local` (and the parameter storages as well), modify it like the following:

```plaintext
"[[p5:params, :local, :server], [p6:params, :local, :server]]"
```

Putting the `server` storage in the outer scope (as in `"[[p5:params, :local], [p6:params, :local], :server]"`) will not work as that will be in a different scope, and visibility from outer scopes don't work.

---

The configuration value format allows the type for repeating declarations to be omitted. The following configurations are the same:

```plaintext
"[[p5:params, l:local], [p6:params, l:local]]"
"[[p5:params, l:local], [p6:params, l:]]"
"[[p5:params, l:], [p6:params, l:local]]"
```

#### `constraint.force.jre.major`

Parameter for overriding the default Java Runtime version [dependency constraint](constraints.md).

The value of the parameter must be an integer that is greater or equals to 1. It will be used to constrain
dependencies and bundle loading instead of the default value. Set it to `"null"` or empty string to
disable the JRE major version constraint.

The default value is the current Java Runtime major version.

#### `constraint.force.architecture`

Parameter for overriding the native architecture [dependency constraint](constraints.md).

The value of the parameter may be any arbitrary string that corresponds to a valid native architecture. (e.g. `"x86"`, `"amd64"`) It is used to constrain dependencies and bundle loading instead of the default value. Set it to `"null"` or empty string to disable the native architecture constraint.

The default value is the current value of the `"os.arch"` system property.

#### `constraint.force.repo.version`

Parameter for overriding the Nest repository version [dependency constraint](constraints.md).

The value of the parameter must be a valid [version number](versioning.md). It will be used to constrain dependencies and bundle loading instead of the default value. Set it to `"null"` or empty string to disable the repository version version constraint.

The default value is the current full version of the Nest repository.

#### `constraint.force.buildsystem.version`

Parameter for overriding the saker.build system version [dependency constraint](constraints.md).

The value of the parameter must be a valid [version number](versioning.md). It will be used to constrain dependencies and bundle loading instead of the default value. Set it to `"null"` or empty string to disable the build system version version constraint.

The default value is the current full version of the saker.build system.

#### `pin.task.version`

Parameter for pinning a specific task version.

The value of the parameter is a semicolon (`;`) separated list that specifies the task names and their
pinned version numbers. When a task lookup request is served by the repository, it will only try to load the task
that has the same version number as the pinned one. Note, that this only happens if the task lookup request has
no version number specified already.

Value example:

```plaintext
my.task:1.0;my.task-q1:1.1;other.task:2.0
```

Extraneous semicolons and whitespace is omitted. The given pin configuration will cause the following build
script to work in the following way:

```sakerscript
my.task()      # will use my.task-v1.0
my.task-v3.0() # will use my.task-v3.0, no override
my.task-q1()   # will use my.task-q1-v1.1
```

If the repository fails to load the task with the pinned version, the lookup will fail, and no other versions
will be searched for.

## Repository storages

See the following documents for different kinds of repository storages and their properties:

<div class="doc-table-of-contents">

* [](serverstorage.md)
* [](localstorage.md)
* [](paramsstorage.md)

</div>
