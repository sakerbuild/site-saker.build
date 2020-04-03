# Bundle format

The bundle contents are packed into a ZIP archive with additional meta-data added to them. (Also known as JAR files.) All bundles must contain a manifest file, and optionally dependency and build task declaration meta-data files.

## Manifest file

The manifest file for the bundle is expected to havethe path `META-INF/MANIFEST.MF`. It must be a well-formatted Java Manifest file, with additional **required** main attributes:

* `Nest-Bundle-Format-Version`: An integer value that specifies the format version of the bundle. Currently the only accepted value is `1`. If the saker.nest repository introduces additional features, other version numbers may be supported later.
* `Nest-Bundle-Identifier`: Specifies the bundle identifier of the bundle. This is the full name of the bundle, including the package name, qualifiers, and a version qualifier. See [](../userguide/index.md#names) for information about the identifier format.

A bundle that contains the above required manifest attributes is the absolute minimal requirements for a valid bundle.

In addition, the following *optional* meta-data attributes can be specified:

* `Nest-Bundle-Source`: Specifies the bundle identifier of the bundle that contains the sources for this bundle.
* `Nest-Bundle-Documentation`: Specifies the bundle identifier of the bundle that contains documentational information for this bundle. The documentation resources are not expected to be in any format, they can be interpreted in an use-case dependent manner.

In addition, the following *optional* behavioural attributes can be specified:

* `Nest-ClassPath-Special`: Specifies special dependencies for the bundle that should be added to the bundle classpath when loaded. The currently accepted only value is `jdktools` which will cause the repository runtime to have the Java tooling classpath available for the bundle classes. (E.g. Java compiler)
* `Nest-ClassPath-Supported-JRE-Versions`: Specifies a [version range](../userguide/versioning.md#version-ranges) that defines the suitable JRE environments that the bundle classes can be loaded on.
* `Nest-ClassPath-Supported-Repository-Versions`: Specifies a [version range](../userguide/versioning.md#version-ranges) that defines the suitable repository versions that the bundle classes can be loaded on.
* `Nest-ClassPath-Supported-Build-System-Versions`: Specifies a [version range](../userguide/versioning.md#version-ranges) that defines the suitable saker.build system versions that the bundle classes can be loaded on.
* `Nest-ClassPath-Supported-Architectures`: Specifies the suitable architectures that the bundle classes can be loaded on in a comma separated list. 

Any other manifest attribute that starts with `Nest-` prefix, and is not present in the above list may be rejected by the repository. They are considered to be reserved attributes.

The standard manifest attribute `Main-Class` can also be declared in the manifest. The repository runtime will read this value and validate that the referenced class is actually contained in the bundle.

## Task file

The tasks in the bundle can be declared in the `META-INF/nest/tasks` meta-data file. The contents of the file is a simple enumeration of the task names paired with the corresponding Java class names in the following format:

```plaintext
[task.name]=[class-name]
```

Each entry should be on its separate line. Extra whitespace around the names and `=` sign are trimmed. Comments are not allowed. The bundle will be validated that it actually contains a class file for the given class name.

When a bundle is uploaded to the saker.nest repository, it will validate that the uploader has claimed all the task names that the bundle declares.

## Dependency file

The dependencies of a bundle can be declared in the `META-INF/nest/dependencies` meta-data file. The format of the dependency file is the following:

The dependencies are declared in 3 levels of white-space indented lines.

The first level contains the name of the bundle identifier that the dependency is declared on:

```plaintext
my.bundle
    ...
second.dependency.bundle-q1-q2
    ....
```

The bundle identifiers may contain qualifiers, but no
meta-qualifiers. Each bundle identifier may only occurr once.
All dependency information for a bundle is written in the block following the bundle identifier.

In each indented block, the indentation characters must be the same. You can use one or more tabs or spaces, but
make sure to use the same indentation throughout an indented block.

A dependency on a bundle consists of one or multiple dependency kinds, a version range, and optional meta-data:

```plaintext
my.bundle
    classpath: 1.0
    kind2, kind3,, kind4: [0)
```

The above shows two dependency declarations. The kind `classpath` is applied with the
[version range](../userguide/versioning.md#version-ranges) of `1.0`. The dependency with kinds `kind2`,
`kind3`, `kind4` is applied with the version range of `[0)`.

A dependency declaration syntax consists of a dependency kind and version range separated by a colon character
(`:`). Multiple kinds can be present separated by commas. Extraneous commas are ignored.

In the above, both dependency declarations are indented with 4 spaces. As mentioned above, this must stay
consistend in a given block.

Dependency meta-datas can be declared for a given declaration:

```plaintext
my.bundle
    classpath: 1.0
        optional: true
        meta2: custom-value
        with-spaces: hello world
        empty:
        quoted: "123"
        quoted: ""word""
        multi-line: "1
2"
        eol-quote-escape: "1"\
2"
        slash: \
        slash-in-str: "\"
```

There's two way the value of a meta-data declaration can be specified. As simple unquoted characters, or in
quotes. Without any quotation, the value will be considered to start from the colon (`:`) and last
until the end of the line. Any white-space will be trimmed from it.

With quotes, the contents of the meta-data value will be all the characters until the first quote that is found
at the end of a line. Intermediate quotes don't need to be escaped. If the value needs to have quotes at the end
of a line and still continue, the backslash (`\`) character needs to be appended to continue parsing
the value.

See the following for multi-line escaping examples:

```plaintext
meta1: "1st
2nd"

meta2: "some"intermediate
"quoted"

meta3: "quotes
at"\
line\\
end"
```

In the above, the meta-data entries will be the following, with new lines as **`\n`**:

* `meta1`: `1st`**`\n`**`2nd`
* `meta2`: `some"intermediate`**`\n`**`"quoted`
* `meta3`: `quotes`**`\n`**`at"`**`\n`**`line\`**`\n`**`end`

If a backslash character is found at the end of a line in a multi-line quoted string value, then it will be
omitted by the parser. Line endings are normalized to `\n`.

As a convenience feature, the parser allows version ranges to contain the `this` identifier. If found,
then it will be substituted by the version number of the declaring bundle identifier for
dependencies that have the same name as the declaring bundle.

E.g. if the declaring bundle is `my.bundle-v1.0` then the following:

```plaintext
my.bundle-q1
    classpath: [this]
```

Will be parsed as if it was the following:

```plaintext
my.bundle-q1
    classpath: [1.0]
```

Note that the `this` identifier will not be substituted if it is declared on a dependency that doesn't
have the same name as the declaring bundle. The following will result in a parsing error:

```plaintext
other.bundle
    classpath: [this]
```

The error occurs as `other.bundle` has a different name than `my.bundle`.

