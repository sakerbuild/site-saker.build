# build

<div class="doc-cmdref-cmd-usage">

build [@command-file] [parameters] target? build-script?

</div>

<div class="doc-cmdref-cmd-doc">

Execute a build with the given parameters.

A build can be configured to run in this process, use a local daemon,
or use a remote daemon.
The build daemon is only used if either the -daemon flag or the -daemon-address
parameter is defined.

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


##### -working-directory

<div class="doc-cmdref-param-aliases">-working-directory &lt;path&gt;
-working-dir &lt;path&gt;
-wd &lt;path&gt;
</div>

<div class="doc-cmdref-param-flags">
</div>

<div class="doc-cmdref-param-doc">

Specifies the working directory for the build execution.

The specified path will be resolved based on the build path configuration.
This means that absolute paths will be based against 
the root directories for the execution, not against the current file system 
that is executing the process.

</div>


##### -build-directory

<div class="doc-cmdref-param-aliases">-build-directory &lt;path&gt;
-build-dir &lt;path&gt;
-bd &lt;path&gt;
</div>

<div class="doc-cmdref-param-flags">
</div>

<div class="doc-cmdref-param-doc">

Specifies the build directory for the build execution.

In general, most of the outputs of the build will be placed in the
specified build directory.
The given path can be absolute or relative. Relative paths will be
resolved against the specified working directory. Absolute paths 
are resolved against the build path configuration. (Similar to 
the working directory.)

</div>


##### -mirror-directory

<div class="doc-cmdref-param-aliases">-mirror-directory &lt;path&gt;
-mirror-dir &lt;path&gt;
-md &lt;path&gt;
</div>

<div class="doc-cmdref-param-flags">
</div>

<div class="doc-cmdref-param-doc">

Specifies the mirror directory for the build execution.

The mirror directory is a path on the local file system, which is used when
files need to be present on the file system. This can be the case when some tasks
need to invoke external processes to execute their work.

If the mirror directory is not specified, the build system will try to use a 
directory under the build directory. If the build directory doesn't reside
on the local file system, no mirror directory will be used by default.

The specified path must be absolute and will be resolved against 
the local file system (that is the file system of the build machine).
(Unlike the working directory.)

</div>


##### -mount

<div class="doc-cmdref-param-aliases">-mount &lt;mount-path&gt; &lt;root-name&gt;
</div>

<div class="doc-cmdref-param-flags">
Multi-parameter. 
</div>

<div class="doc-cmdref-param-doc">

Mounts a given directory for the build execution.

The mounted directory will be added to the build execution, and it will be
available for the tasks as a root directory for files.

The arguments for this parameter is in the following format:

    name://absolute/path/to/directory mounted:
    
In which case the the "/absolute/path/to/directory" on the connection "name"
will be mounted as the root directory "mounted:".

For Windows file system paths the path can be specified as:

    name:/c:/absolute/path
    
Where "name" is the connection name and "c:" is the drive letter for the
absolute path.

The "name:/" part can be omitted, in which case the remaining part will be 
interpreted for the local file system.

If the name "local" is specified, the remaining absolute path is interpreted
for the local file system.
If the name "remote" is specified, the remaining absolute path is interpreted
for the file system of the daemon (if any) that is running the build.
If the name "pwd" is specified, the remaining absolute path names are 
interpreted against the process working directory.

Can be used multiple times to mount multiple directories.

</div>


##### -U

<div class="doc-cmdref-param-aliases">-U&lt;key&gt;=&lt;value&gt;
</div>

<div class="doc-cmdref-param-flags">
Multi-parameter. 
</div>

<div class="doc-cmdref-param-doc">

Species user parameters for the build execution.

User parameters are arbitrary key-value pairs which can be used to dynamically
configure different aspects of the build system. This is usually applicable for
configuring repositories or specific tasks you're executing.

Can be used multiple times to define multiple entries.

</div>


##### -repository-no-nest

<div class="doc-cmdref-param-aliases">-repository-no-nest
-repo-no-nest
</div>

<div class="doc-cmdref-param-flags">
</div>

<div class="doc-cmdref-param-doc">

Flag to specify that the saker.nest repository shouldn't be 
automatically included for the build.

The saker.nest repository is the default repository associated with the build
system. Specify this flag to disable its automatic inclusion.

</div>


##### -repository

<div class="doc-cmdref-param-aliases">-repository &lt;classpath&gt;
-repo &lt;classpath&gt;
</div>

<div class="doc-cmdref-param-meta">
<span class="dod-cmdref-param-meta-name">[repository]</span></div>

<div class="doc-cmdref-param-flags">
Multi-parameter. 
</div>

<div class="doc-cmdref-param-doc">

 Starts a repository configuration with a given classpath.

 The classpath may be an HTTP URL by starting it with the 
 'http://' or 'https://' phrases. 
 It can also be a file path in the format specified by -mount. 
 The paths are resolved against the path configuration of the build.
 
 It can also be in the format of 'nest:/version/&lt;version-number&gt;'
 where the &lt;version-number&gt; is the version of the saker.nest repository 
 you want to use. The &lt;version-number&gt; can also be 'latest' in which 
 case the most recent known saker.nest nest repository release is used.
 
 Any following -repository parameters will modify this configuration.

</div>


##### -repository-class

<div class="doc-cmdref-param-aliases">-repository-class &lt;class name&gt;
-repo-class &lt;class name&gt;
</div>

<div class="doc-cmdref-param-meta">
<span class="dod-cmdref-param-meta-name">[repository]</span></div>

<div class="doc-cmdref-param-flags">
Multi-parameter. 
</div>

<div class="doc-cmdref-param-doc">

Specifies the name of the repository class for the previously
started repository configuration.

The class should be an instance of 
saker.build.runtime.repository.SakerRepositoryFactory.

If not specified, the service configuration of the classpath will 
be used to load the repository. See the ServiceLoader Java class
for more information.

</div>


##### -repository-id

<div class="doc-cmdref-param-aliases">-repository-id &lt;string&gt;
-repo-id &lt;string&gt;
</div>

<div class="doc-cmdref-param-meta">
<span class="dod-cmdref-param-meta-name">[repository]</span></div>

<div class="doc-cmdref-param-flags">
Multi-parameter. 
</div>

<div class="doc-cmdref-param-doc">

Specifies the identifier of the repository for the previously
started repository configuration.

The repository identifier should be unique for each repository
and they can be used to differentiate them in appropriate contexts.

E.g. when invoking tasks, the &#064;repositoryid syntax can be used to
specify where to look for the task implementation.

</div>


##### -script-files

<div class="doc-cmdref-param-aliases">-script-files &lt;wildcard&gt;
</div>

<div class="doc-cmdref-param-meta">
<span class="dod-cmdref-param-meta-name">[script]</span></div>

<div class="doc-cmdref-param-flags">
Multi-parameter. 
</div>

<div class="doc-cmdref-param-doc">

Starts a script configuration that applies to the files specified
by the parameter wildcard.

Script configurations specify how a given build script should be
parsed when used during the build. If the specified wildcard matches
the full absolute path of the build script, then it will be used
to parse the script.

Scripts configurations allow different languages to be used and enable
configuring custom options for them.

Any following -script and -SO parameters will modify this configuration.

</div>


##### -script-classpath

<div class="doc-cmdref-param-aliases">-script-classpath &lt;classpath&gt;
-script-cp &lt;classpath&gt;
</div>

<div class="doc-cmdref-param-meta">
<span class="dod-cmdref-param-meta-name">[script]</span></div>

<div class="doc-cmdref-param-flags">
Multi-parameter. 
</div>

<div class="doc-cmdref-param-doc">

Specifies the classpath of the script parser for the
previously started script configuration.

The classpath is in the same format as in -repository parameter.

</div>


##### -script-class

<div class="doc-cmdref-param-aliases">-script-class &lt;class name&gt;
</div>

<div class="doc-cmdref-param-meta">
<span class="dod-cmdref-param-meta-name">[script]</span></div>

<div class="doc-cmdref-param-flags">
Multi-parameter. 
</div>

<div class="doc-cmdref-param-doc">

Specifies the class name of the script parser for the
previously started script configuration.

The class should be an instance of 
saker.build.scripting.ScriptAccessProvider.

If not specified, the service configuration of the classpath will 
be used to load the parser. See the ServiceLoader Java class
for more information.

</div>


##### -SO

<div class="doc-cmdref-param-aliases">-SO&lt;key&gt;=&lt;value&gt;
</div>

<div class="doc-cmdref-param-meta">
<span class="dod-cmdref-param-meta-name">[script]</span></div>

<div class="doc-cmdref-param-flags">
Multi-parameter. 
</div>

<div class="doc-cmdref-param-doc">

Specifies a script option for the previously started script configuration.

Script options are string key-value pairs that are interpreted by the
script parser in an implementation dependent way.

See the documentation of the associated scripting language to see
what kind of options they accept.

</div>


##### -dbconfig-fallback

<div class="doc-cmdref-param-aliases">-dbconfig-fallback &lt;type&gt;
</div>

<div class="doc-cmdref-param-flags">
</div>

<div class="doc-cmdref-param-doc">

Specifies the file content change detection mechanism for the
build database.

The specified mechanism will be used to determine if the contents
of a given file has been changed between build executions. Accepted
values are the following:
    attr
        The file attributes and size will be used to compare the state
        of the file.
        This is the default value for the build system.
    md5
        The contents of the files will be hashed using MD5, and the 
        hash will be compared to the previous state of the file.

This parameter specifies the mechanism for the files which have not
been matched by any of the wildcards specified using -dbconfig-path.

To specify a mechanism for all files, use this parameter, and don't
specify anything for -dbconfig-path.

</div>


##### -dbconfig-path

<div class="doc-cmdref-param-aliases">-dbconfig-path &lt;connection-name&gt; &lt;wildcard&gt; &lt;type&gt;
</div>

<div class="doc-cmdref-param-flags">
Multi-parameter. 
</div>

<div class="doc-cmdref-param-doc">

Specifies the file content change detection mechanism for the
build database for the files matched by the given wildcard.

The &lt;type&gt; argument accepts the same values as -dbconfig-fallback.

The &lt;wildcard&gt; applies to all files that is accesible through the
given connection. 

The &lt;connection-name&gt; parameter is interpreted in 
the same way as in name part of -mount parameter paths.

</div>


##### -cluster

<div class="doc-cmdref-param-aliases">-cluster &lt;connection-name&gt;
</div>

<div class="doc-cmdref-param-flags">
Multi-parameter. 
</div>

<div class="doc-cmdref-param-doc">

Specifies a daemon connection that should be used as a cluster for the build.

This parameter adds connection specified by the given name as cluster to
the build execution. Clusters can be used to execute a build over multiple
computers by delegating tasks to it.

Note, that only those tasks can be delegated to clusters which actually
support this feature.

Connections can be specified using the -connect parameter.

This parameter can be specified multiple times.
Specifying a connection multiple times as a cluster has no additional
effects.

</div>


##### -cluster-use-clients

<div class="doc-cmdref-param-aliases">-cluster-use-clients
</div>

<div class="doc-cmdref-param-flags">
</div>

<div class="doc-cmdref-param-doc">

Flag to specify to use the connected clients of the used daemon as clusters.

If the daemon that is used for build execution has other clients connected
to it, then they will be used as clusters for this build execution.

The default is false.

</div>


##### -connect

<div class="doc-cmdref-param-aliases">-connect &lt;address&gt; &lt;name&gt;
</div>

<div class="doc-cmdref-param-flags">
Multi-parameter. 
</div>

<div class="doc-cmdref-param-doc">

Specifies a connection to a given address and identifies it by the specified name.

A connection will be established to the build system daemon 
running at the given address. The connection can be used to add 
file systems to the build, run the build itself on that daemon, or others.

The established connection can be referenced by its name in other parameters.
The names for the connections must be unique, only one connection 
can be specified for a given name.

This parameter can be specified multiple times.

Connection names which represent well known URL protocols or identifiers,
may be reserved and an exception will be thrown. Such names are:

    local, remote, http, https, file, jar, ftp and others...
      (The list of names may change incompatibly in the future.)

If you encounter incompatibility, simply choose a different name for your
connection.

The default port for the addresses is 3500.

</div>


##### -daemon

<div class="doc-cmdref-param-aliases">-daemon
</div>

<div class="doc-cmdref-param-flags">
</div>

<div class="doc-cmdref-param-doc">

Flag for specifying if a daemon should be used for build execution.

If this flag is specified, a daemon on the local machine will
be used for the execution of the build. If the daemon is not 
running, it will be started with the properties specified by
other -daemon parameters.

If a daemon is already running, then that will be used if the configuration
of it is acceptable. If there is a configuration mismatch between
the expected and actual configurations, an exception will be thrown.

If -daemon-address is also specified, this flag specifies that
a daemon process on the local machine can be started if necessary.
The -daemon parameters will be used when starting the new daemon.

</div>


##### -daemon-address

<div class="doc-cmdref-param-aliases">-daemon-address &lt;address&gt;
</div>

<div class="doc-cmdref-param-flags">
</div>

<div class="doc-cmdref-param-doc">

Specifies the address of the daemon that should be used for build execution.

The default port for the address is 3500.

If this parameter is specified, a local daemon will only be instantiated
if required and the -daemon flag is specified.

Specifying a loopback or local address is the same as specifying the 
-daemon flag.

See -daemon parameter flag.

</div>


##### -daemon-port

<div class="doc-cmdref-param-aliases">-daemon-port &lt;int[0-65535]&gt;
</div>

<div class="doc-cmdref-param-flags">
</div>

<div class="doc-cmdref-param-doc">

Specifies the port on which the daemon should listen for incoming connections.

If the port is 0, it will be assigned by the operating system, therefore 
it may be random.

If not specified, the default port of 3500 will be used.

</div>


##### -daemon-storage-directory

<div class="doc-cmdref-param-aliases">-daemon-storage-directory &lt;path&gt;
-daemon-storage-dir &lt;path&gt;
-daemon-sd &lt;path&gt;
</div>

<div class="doc-cmdref-param-flags">
</div>

<div class="doc-cmdref-param-doc">

Specifies the storage directory that the build environment can use
to store its files and various data.

Note that only a single daemon can be running in a given storage directory.

</div>


##### -daemon-server

<div class="doc-cmdref-param-aliases">-daemon-server
</div>

<div class="doc-cmdref-param-flags">
</div>

<div class="doc-cmdref-param-doc">

Flag that specifies whether or not the daemon should act as a server.
A server daemon can accept incoming connections from any network addresses.
A non-server daemon can only accept connections from the localhost.

</div>


##### -daemon-cluster-enable

<div class="doc-cmdref-param-aliases">-daemon-cluster-enable
</div>

<div class="doc-cmdref-param-flags">
</div>

<div class="doc-cmdref-param-doc">

Flag that specifies if the daemon can be used as a cluster for build executions.

By default, daemons may not be used as clusters, only if this flag is specified.
Clusters can improve the performance of build executions by enabling distributing
various tasks over a network of build machines.

</div>


##### -daemon-cluster-mirror-directory

<div class="doc-cmdref-param-aliases">-daemon-cluster-mirror-directory &lt;path&gt;
-daemon-cluster-mirror-dir &lt;path&gt;
-daemon-cmirrord &lt;path&gt;
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


##### -no-daemon-cache

<div class="doc-cmdref-param-aliases">-no-daemon-cache
</div>

<div class="doc-cmdref-param-flags">
</div>

<div class="doc-cmdref-param-doc">

Flag for specifying that the build execution should not use any caching
for keeping the project-related data in memory.

When this flag is not used and the build is executed via a build daemon, some
project related data will be cached for some amount of time for faster
incremental builds.
Specifying this flag will turn that off and the build will not use
cached information for its execution.

</div>


##### -stacktrace

<div class="doc-cmdref-param-aliases">-stacktrace &lt;enum&gt;
</div>

<div class="doc-cmdref-param-flags">
</div>

<div class="doc-cmdref-param-doc">

Specifies the format of the stack trace that is printed in case of build failure.

Possible values are:

- full
  - Every stack trace element and script trace will be printed.
- reduced
  - Some internal stack traces will be removed, but the number of removed
      frames will be included in the stack trace.
- compact
  - Interal stack traces are removed, and no indicator of such removal
      is displayed on the printed information.
- script_only
  - Only script traces are displayed for the exceptions.
      No Java stack traces are displayed.
- java_trace
  - Only Java stack traces are displayed for the exceptions.
      No build script traces are displayed.

</div>


##### -interactive

<div class="doc-cmdref-param-aliases">-interactive
</div>

<div class="doc-cmdref-param-flags">
</div>

<div class="doc-cmdref-param-doc">

Flag for specifying to run in interactive mode.

Interactive mode entails that the build tasks may read input
from the console. It usually means that a developer actively
monitors the build.

This can be used for local builds and is not recommended
for builds running on a Continuous Integration server or where
the developer has no opportunity to provide manual input.

</div>


##### -trace

<div class="doc-cmdref-param-aliases">-trace &lt;mount-path&gt;
</div>

<div class="doc-cmdref-param-flags">
</div>

<div class="doc-cmdref-param-doc">

Sets the output path of the build trace for the build execution.

The path is expected to be in the same format as in the -mount 
parameter.

The build trace can be viewed in a browser, by navigating to:
    https://saker.build/buildtrace
and opening it on the page.
(The build trace can be viewed offline, it won't be transferred 
to our servers.)

</div>


##### -trace-artifacts-embed

<div class="doc-cmdref-param-aliases">-trace-artifacts-embed
</div>

<div class="doc-cmdref-param-flags">
</div>

<div class="doc-cmdref-param-doc">

Instructs the build trace to embed the output build artifacts
in the created build trace file.

If this flag is specified, the build artifacts that the 
build tasks produce will be embedded in the build trace. They
can be downloaded directly from the build trace view.

</div>


##### target

<div class="doc-cmdref-param-format">&lt;string&gt;</div>

<div class="doc-cmdref-param-flags">
Positional parameter. 
</div>

<div class="doc-cmdref-param-doc">

The build target to execute.

If not specified then defaults to the following:

- If there is only one target in the build file then that one is invoked.
- The target with the name "build" is invoked if exists.
- An exception is thrown otherwise.

</div>


##### build-script

<div class="doc-cmdref-param-format">&lt;path&gt;</div>

<div class="doc-cmdref-param-flags">
Positional parameter. 
</div>

<div class="doc-cmdref-param-doc">

The build script file to invoke the target of.

Defaults to the script with the name "saker.build" in the working directory.
If that one doesn't exist, then a build script is selected only if there is only
one build script in the working directory.
An exception is thrown if a build script file is not found.

</div>

