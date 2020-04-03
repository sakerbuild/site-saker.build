# Path configuration

One important aspect of saker.build system is that it maintains its own file tree representation for the files that it works with. This entails that the files are accessible using different paths than the actual file system that the build is running on.

Users can configure the paths in the build system to point to various locations on the local or remote file systems. This enables running a build that uses files from multiple computers and making them transparently accessible.

All absolute paths in the build system have a root name and the specified path names associated with it. The root names can be in the following format:

* Simple `/` character which matches the Unix root drive semantics.
* A named drive root name which contains lowercase characters in the range of `'a'` - `'z'`. This matches the Windows root drive semantics.
	* E.g. `wd:` for working directory, `bd:` for build directory, and other arbitrary names that can be specified by the user.

Each build execution is configured to have a working directory and at least one mounted directory. The working directory must always be a path that is accessible from one of the provided roots.

If you run a build without any explicit path configuration, it will be based on the current working directory of the Java process. For example:

If the current working directory of the build process is `/home/dev`, the working directory root for the build execution will be: `wd:` . It will be mapped to the `/home/dev` path.

The build execution roots will be the following:
```
wd: -> /home/dev
```

This way, if you want to reference an absolute path in the build system, you can't use `/home/dev/myfile`, but need to use `wd:/myfile`, as the build system has no notion of the `/` root specified in the former path.

Another note is that the build tasks will not have access to the files which are not accessible from any of the roots specified. (Note that this is not a security guarantee, but only a semantic one.)

You can have any configuration which suit your needs, let's look at an example which separates the sources, resources, build directory, and project directory:

Current working directory of the build process: `/home/dev`\
Build working directory: `wd:` (that is mapped accordingly to `/home/dev/project`)\
The build system roots:
```
wd: -> /home/dev/project
bd: -> /home/dev/build
sources: -> /home/dev/sources
res: -> /home/dev/res
```

The following command line arguments can be used to achieve the above configuration:

```
-build-dir bd:
-working-dir wd:
-mount pwd://project wd:
-mount pwd://build bd:
-mount pwd://sources sources:
-mount pwd://res res:
```

See the [command line reference](cmdlineref/build.md) for information about the above arguments.

Note that the above arguments depend on the current process working directory (pwd) of the process being `/home/dev`. You may specify absolute paths when mounting directories, or even different machines over the network. (See the `-connect` argument.)

Note that the build system will disallow any path configuration that result in a file being accessible via multiple root directories. You cannot mount a directory and any of its parent in a valid path configuration.

## Build directory

When you invoke a build execution and specify no build directory, the build system will not automatically set one for you. This is in order to avoid accidental overwriting of user files.

We recommend always setting the build directory for the execution, as otherwise the tasks may not have a location to put their result files in, or may unexpectedly throw exceptions. Use the [`-build-dir`](/doc/guide/cmdlineref/build.md#-build-directory) command line option to set a build directory for your execution.

## Mirror directory

The mirror directory is a directory on the local file system that is used by tasks if any file needs to be referenced from the actual file system rather than the in-memory file hierarchy.

The mirror directory is often used when external processes needs to access files which are present in the build system file hierarchy. It is always on the file system of the machine that executes the build.

An example use-case for it is when a build task invokes external C/C++ compiler. It will mirror the file to a location under the mirror directory, and pass the path of the mirrored file to the compiler. After the compilation is done, the task can examine the results of the compilation.

If the mirror directory is not specified, the build system will attempt to use a directory named `mirror` under the specified build directory, if any. If the build directory is not associated with a local file system directory, no mirror directory will be used.

Not specifying a mirror may result in exceptions during build execution as some tasks may require it.

Mirror directories can also be specified (and highly recommended) for [build clusters](buildclusters.md#mirror-directory).
