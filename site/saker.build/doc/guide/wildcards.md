# Wildcard paths

Wildcard paths are used to specify a collection of files that match a given pattern. It is often referred as [globs](https://en.wikipedia.org/wiki/Glob_(programming)) in other contexts.

The build system uses wildcard paths in various configurations and also allows tasks to take advantage of this feature. \
Some of the command line arguments use these wildcards to specify the files they apply to (e.g. [script language configuration](scriptlanguages.md)).\
Task implementations are encouraged to support wildcard paths in order to make it convenient for users to add new source and resource files without the need of modifying the build files.

The wildcard implementation of saker.build works as follows:

A wildcard path is an either relative or absolute path that may include special characters (`*`, `**`) which are handled specially by the implementation. The characters of `?`, `[`, `]` are unused but reserved in case future implementations may want to add functionality for those.

The single `*` character will match 0 or more arbitrary characters in a path name but doesn't cross directory boundaries.\
The double `**` when used as a single path name will match 0 or more arbitrary directory names.

Some examples for wildcard paths:

 * `dir/no/wildcard` only matches `dir/no/wildcard`
 * `abs:/no/wildcard` only matches `abs:/no/wildcard`
 * `/no/wildcard` only matches `/no/wildcard`
 * `dir/*.ext` matches files under the directory `dir` which have the extension `.ext`
 * `dir/*123*` matches files under the directory `dir` which contain the number `123`
 * `dir/**/*.ext` recursively matches files under the directory `dir` with the extension `.ext`
 * `dir/**.ext` is the same as `dir/*.ext`
 * `dir/**` recursively matches all files under the directory `dir` (children and their children too)
 * `*d:/` will match root directories whose drive ends with `d`

The wildcard paths are considered to be case sensitive. The wildcards doesn't handle specially the `.` and `..` path names. It is usually semantically invalid to include them in a wildcard.
