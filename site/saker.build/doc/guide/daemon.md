# Build daemon

A build daemon is a long running background process that can accept requests from clients to execute various tasks.

The main purpose of a daemon is to reduce the initialization times for build executions. As the Java Virtual Machine (JVM) can take some time to warm up and properly optimize the loaded classes, keeping a running instance in the background can significantly speed up the time it takes to start and execute a build.

The daemons can also cache project-specific data in order to avoid loading the build database, the configuration, and other values from the disk every time. It also speeds up the delta detection of file changes in the project, by installing file system watcher(s) that invalidates the file tree if changes are detected. (Note that it doesn't use polling, so it uses little resources in order to do this, even on macOS, where the proper implementation of the file system watcher is [lacking](https://bugs.openjdk.java.net/browse/JDK-7133447).)

Daemons can be configured to accept build request over the network, be used as a [build clusters](buildclusters.md), serve a [build cache](buildcache.md) (WIP), or just provide access to the associated file system. They are basically the core build system in a long running process with the additional benefit that they can work together when necessary.

## Project caching

The daemon caches resources related to a given project. These resources include Java classes used during the build execution of the project, the file hierarchy, persistent build resources, build database and others. The daemon also uses file watchers on the file systems to detect any changes occurring to the interested files as they happen rather than reevaluating the differences when a build is started.

This caching mechanism allows to have a significantly shorter startup time of a build and also allows the results of the build to be available sooner. When a build is finished, any build database and other flushing occurs after the build finishes, therefore providing a quicker feedback to the user.

## Examples

The following are examples of running the daemon or executing builds that use the build daemon. As a baseline we recommend the reading the related part from the [command line reference](cmdlineref/daemon.md).

### Starting the daemon

Starting the build daemon can be done explicitly, or implicitly. Explicitly when using the following command line:

```
daemon start
```

This will start the daemon in a new Java process and ensure that it has properly bootstrapped. Without any additional parameters, the daemon will use the default parameters. If the daemon is already running, an appropriate message will be displayed to the user. If it is running with the same parameters, the process will exit with 0, if the parameters are different, an exception is thrown (i.e. non-zero status code).

See the [command line reference](cmdlineref/daemon_start.md) for parameter information.

The daemon can be also started implicitly, when you start a build execution with the [`-daemon`](cmdlineref/build.md#-daemon) flag.

```
build -daemon [...]
```

This way, a daemon will be used when building your project. If the daemon is already running, it will be used, otherwise it will be started with the appropriate parameters. See [build command line reference](cmdlineref/build.md) for more information.

You can also start the daemon without spawning another process with the [`daemon run`](cmdlineref/daemon_run.md) command:

```
daemon run
```

This way the daemon will run directly in the process. Any standard output and error will be displayed in your console. It can be beneficial to run the daemon this way if you wish to debug the Java build process (useful when developing extensions). See the [command line reference](cmdlineref/daemon_run.md) for more information.

### Daemon status

You can check the status of a running daemon by using the [`daemon info`](cmdlineref/daemon_info.md) command:

```
daemon info -address [...]
```

The [`-address`](cmdlineref/daemon_info.md#-address) specifies the target IP address or host name of the daemon. If it is omitted, the `localhost` is used. The command will print out basic information about the running daemon, or throw an exception if failed to connect.

For other debugging purposes, the [`daemon io`](cmdlineref/daemon_io.md) command can be also used.

### Stopping the daemon

The daemon can be stopped with the [`daemon stop`](cmdlineref/daemon_stop.md) command. It works similarly to the [status](#daemon-status) checking, but stops the daemon instead.

```
daemon stop -address [...]
```
