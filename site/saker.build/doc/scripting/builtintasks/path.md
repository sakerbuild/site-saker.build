# `path()`

The `path()` task can be used to retrieve an absolute path to the working directory of the build script or resolve a relative path against it.

The working directory of the build script is usually the directory in which the build script resides in. (I.e. the parent directory) However, the build targets can be invoked in a way that this differs from the parent directory.

A good use-case for this task is when a relative path name might be used from a different script file. The task can convert the path name in the originating script to absolute, so the resolution of relative path names doesn't yield an unexpected path.

## Parameters

The following parameters are used by the task:

| Parameter 	       	| Description   	|
|----------------------	|---------------	|
| unnamed	| *Optional* path to resolve against the working directory of the build script.	|

If no parameters are specified, the task will return the working directory of the script file.

If the unnamed path parameter is given and relative, it will be resolved against the current working directory. If the path parameter is absolute, it will be returned as the result without modification.

## Task result

The path to the working directory if no arguments specified, else the argument path resolved against the current working directory.

## Example

The following example is contained in a file at the path `wd:/project/saker.build`:

`wd:/project/saker.build`:

```sakerscript
# the build script path is: wd:/project/saker.build
# the build target is invoked with wd:/project as working directory

# equals wd:/project
path()
# equals wd:/project/file.txt
path(file.txt)
# equals wd:/parentfile.txt
path(../parentfile.txt)

# equals /absolute/path
path(/absolute/path)

# error, cannot be resolved as it would escape the root
path(../../error.txt)
```
