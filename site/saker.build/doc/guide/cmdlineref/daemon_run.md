# run

<div class="doc-cmdref-cmd-usage">

daemon run [@command-file] [parameters]

</div>

<div class="doc-cmdref-cmd-doc">

Runs the build daemon in this process with the given parameters.

This command can be used to run the daemon manually, when you don't want the
build system to automatically manage its lifetime.

This command can also be used to start a daemon with debugging enabled, and
connect to it via a debug client. One example for it is:

    java -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=1044 -jar ...

After starting it you can use the debugger of your choosing to debug the daemon process.

</div>

##### @command-file

<div class="doc-cmdref-param-aliases">@command-file
</div>

<div class="doc-cmdref-param-flags">
</div>

<div class="doc-cmdref-param-doc">

File path prefixed with '@' to directly include arguments from the 
specified file. Each argument is on its separate line. They are
directly inserted in place of the @command-file argument. 
The argument can appear anywhere on the command line. Escaping
is not supported for arguments in the command file. 
The file path may be absolute or relative.

E.g: @path/to/arguments.txt

</div>


##### -storage-directory

<div class="doc-cmdref-param-aliases">-storage-directory &lt;path&gt;
-storage-dir &lt;path&gt;
-sd &lt;path&gt;
</div>

<div class="doc-cmdref-param-flags">
</div>

<div class="doc-cmdref-param-doc">

Specifies the storage directory that the build environment can use
to store its files and various data.

Note that only a single daemon can be running in a given storage directory.

</div>


##### -server

<div class="doc-cmdref-param-aliases">-server
</div>

<div class="doc-cmdref-param-flags">
</div>

<div class="doc-cmdref-param-doc">

Flag that specifies whether or not the daemon should act as a server.
A server daemon can accept incoming connections from any network addresses.
A non-server daemon can only accept connections from the localhost.

</div>


##### -port

<div class="doc-cmdref-param-aliases">-port &lt;int[0-65535]&gt;
</div>

<div class="doc-cmdref-param-flags">
</div>

<div class="doc-cmdref-param-doc">

Specifies the port on which the daemon should listen for incoming connections.

If the port is 0, it will be assigned by the operating system, therefore 
it may be random.

If not specified, the default port of 3500 will be used.

</div>


##### -cluster-enable

<div class="doc-cmdref-param-aliases">-cluster-enable
</div>

<div class="doc-cmdref-param-flags">
</div>

<div class="doc-cmdref-param-doc">

Flag that specifies if the daemon can be used as a cluster for build executions.

By default, daemons may not be used as clusters, only if this flag is specified.
Clusters can improve the performance of build executions by enabling distributing
various tasks over a network of build machines.

</div>


##### -cluster-mirror-directory

<div class="doc-cmdref-param-aliases">-cluster-mirror-directory &lt;path&gt;
-cluster-mirror-dir &lt;path&gt;
-cmirrord &lt;path&gt;
</div>

<div class="doc-cmdref-param-flags">
</div>

<div class="doc-cmdref-param-doc">

Specifies the mirror directory when the daemon is used as a cluster.

The cluster mirror directory is used by executed tasks to cache and use files
on the local file system. As many tasks may require the files to be present
on the local file system for invoking external processes on it, it is strongly
recommended to have a mirror directory for clusters.

Specifying this is not required, but strongly recommended. The directory can
be any directory on the local file system. If you specify this flag, the
daemon takes ownership of the contents in the directory, and may delete files in it.

</div>


##### -EU

<div class="doc-cmdref-param-aliases">-EU&lt;key&gt;=&lt;value&gt;
</div>

<div class="doc-cmdref-param-flags">
Multi-parameter. 
</div>

<div class="doc-cmdref-param-doc">

Specifies an environment user parameter for the build environment.

The build environment will be constructed with the specified user parameters.
The user parameters may be arbitrary key-value pairs that can be used to
configure different aspects of the build environment. They are usually used
to specify the properties of the machine the environment is running on.
E.g. tooling install locations, version informations, etc...

Can be used multiple times to define multiple entries.

</div>


##### -thread-factor

<div class="doc-cmdref-param-aliases">-thread-factor &lt;int&gt;
</div>

<div class="doc-cmdref-param-flags">
</div>

<div class="doc-cmdref-param-doc">

Sets the thread factor for the build environment.

The thread factor is a hint for the build environment to set the recommended
number of threads when dealing with multi-threaded worker threads.

If unspecified, 0, or negative, the thread factor will be determined in an
implementation dependent manner. (Usually based on the number of cores the CPU has.)

</div>


##### -no-output

<div class="doc-cmdref-param-aliases">-no-output
</div>

<div class="doc-cmdref-param-flags">
</div>

<div class="doc-cmdref-param-doc">

Specify this flag to not display the standard output and error of the process.

</div>


##### -saker-jar

<div class="doc-cmdref-param-aliases">-saker-jar &lt;path&gt;
</div>

<div class="doc-cmdref-param-flags">
</div>

<div class="doc-cmdref-param-doc">

Specifies the location of the build system runtime.

The build system requires its distribution JAR for proper operation,
as it may be necessary for some tasks to start new processes 
to do their work.

Under normal circumstances the build system can locate the appropriate
JAR location based on the classpath of the current process. If it fails,
an exception will be thrown and you might need to specify this if required.

The path will be resolved against the local file system, relative paths
are resolved against the working directory of the process.

(If you ever encounter a bug in automatic resolution, please file
an issue at https://github.com/sakerbuild/saker.build/issues)

</div>

