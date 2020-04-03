# File change tracking

When multiple builds are run in succession, the changes in the contents of files needs to be determined by the build system. Generally this can be done in various ways and saker.build allows you to configure it accordingly.

The build system currently supports the following change detection mechanisms:

* Using file attributes (default)
	* The last modification time and size of a file is recorded when it is accessed during build execution. They are stored in the build database and when a new build is started, they are compared to the current attributes of the file. If there is any change for the last modification time or file size, the file is considered to be changed.
* MD5 Hash of the file contents
	* The build system will read the files fully and construct a hash from the contents using the MD5 algorithm. When a new build is started, the hashes of the files are compared, and the change is only detected if they're different.

**Which one should you use?**

In most use-cases you're probably fine with using file attributes. However, file systems may have a lower precision tracking of the last modification time of the files, so modifying files rapidly could cause the changes being unnoticed. We haven't experienced this during testing, but if you encounter such problems, consider using hashes instead. (Note that the size of the file is tracked as well, so this behaviour further lowers the chances of this happening.)

When you're using build caches, you are strongly recommended to use hashes instead of attributes. This is because build caching may not work properly if the attributes of a file is associated with its contents. Version control systems and other tools may not necessarily preserve the attributes therefore the file could be detected as changed even if its not. See [](buildcache.md) for more information.

## Configuration

You can configure two aspects of the file change tracking. One is by specifying a change detection mechanism for files matched by a given wildcard path, and by setting a fallback mechanism that is used when no other configurations can be matched.

The default mechanism uses file attributes to detect changes.

To specify using hashes, use the following command line argument:

```
-dbconfig-fallback md5
```

This will result in the build system using hashes to detect the content related changes in all accessed files.

---

To specify different mechanisms for different files, use the following:

```
-dbconfig-path local **/*.cpp md5
-dbconfig-path local **/*.java md5
```

The above will result in the build system using hashes for `cpp` and `java` source files while using attributes (the default fallback mechanism) for all other files. The `local` keyword specifies that the configuration should be applied to the files residing on the local machine.

**Important** to note that these configurations are independent from the [path configuration](pathconfiguration.md) of the build execution. The specified wildcards are used with the full absolute paths on the associated file system of a given file. Users should take care when executing builds that use multiple machines and clusters.
