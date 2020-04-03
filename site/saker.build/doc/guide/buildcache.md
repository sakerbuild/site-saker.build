# Build cache

<div class="doc-wip">

The following is still work in progress. Main functionality of build caching is done, tests are successful, but the implementation for a persistent cache database is still yet to be done. (I.e. there's only an in-memory database implementation available, which is only suitable for testing purposes.)

</div> 

Build caching allows developers to retrieve the results of the build tasks from a common source instead of running them again. Using it is most advantageous with large project and multiple people working on it. Usually, build caches have limited use for small-medium sized projects with few developers.

Using a build cache the developer workflow is usually the following:

1. Edit the code and push it to source control.
2. As the shared source repository is updated, a continuous integration server will build the project.
3. The continuous integration server uploads the cacheable build tasks to a shared build cache.
4. A developer cleanly checks out the code from source control and builds it on their local machine.
	1. The build uses the build cache shared by the organization
	2. The build execution will retrieve the results of cacheable tasks from the build cache, and skips the actual compilation on the local machine.
	3. As a result, the first build on the local machine will take much less time than a build without caching.

Using and maintaining a build cache however, requires a few configuration changes to your builds. First of all, the build execution should be configured in a way that uses the build cache.

The cacheable tasks need to be published to the build cache so their results can be reused. It is strongly recommended to only publish the results of the tasks from continuous integration or other similar services. The Edit, Build, Debug cycle of developers could unnecessarily pollute the cache with intermediate results which are probably never going to be used.

One of the most important aspects is that the execution should be configured to use the hash based [file change tracking](filechangetracking.md) mechanism. If it is not configured accordingly, the cached results of a task might not be properly found in the cache. File attribute based content tracking is not unique to the contents, therefore it might be missed. Hashes provide a solution for this. The following example should shine some light on why attribute based tracking is not suitable:

1. A developer edits `Main.java` source file, and pushes it to source control.
2. The continuous integration server compiles, and publishes the results of the compilation. Let's assume that the last modification time of `Main.java` is *Monday*.
3. The next day, the developer checks out the project from source control, and builds it. After the checkout, the last modification time of `Main.java` will be *Tuesday*.
	1. The task to compile `Main.java` will be searched for in the build cache.
	2. The build cache contains an entry for `Main.java` with the last modification time of *Monday*. Currently, the modification time of `Main.java` is *Tuesday*, therefore the cache entry will **not** be found, although the contents of the files are the same.
	3. The compilation will occurr again on the local machine of the developer, although there could be an usable cache entry.

The above scenario is solved by using hash based content detection, because the build cache entry will be found, as the hash of `Main.java` is the key for the cache entry, not its modification time.

Another thing to note is that the build system will only search for a cache entry if a given task is being run for the first time. I.e. the task is part of a build from a clean state. This is in order to avoid unnecessary network connections to stall the build in the Edit, Build, Debug cycle where the build should be as fast as possible. It is recommended to clean the project build results when expecting build cache use. *This behaviour may change in the future.*
