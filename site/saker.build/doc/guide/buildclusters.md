# Build clusters

Build clusters are remote daemons that can accept tasks to execute on them. In practice this means that the tasks which are executed during a build can be offloaded to other computers and therefore using more of the available resources. This can significantly increase the performance for large projects.

Saker.build natively supports running and using build clusters. It is a part of the [daemon](daemon.md) functionality, and can be used accordingly. However, only tasks which themselves support build clusters are dispatched to remote machines. Make sure you read the documentations of the given tasks to check if they support clusters.

## Requirements

Using a machine as a build cluster has no additional requirements. They can be configured completely independently than the build they are attached to. They can even be different operating systems, and have different build environment overall.

The tasks that are dispatched to build clusters will appropriately choose the cluster they can run on. E.g. if a task needs to run on macOS, then it will only run on a build cluster that runs on a macOS operating system. If none is suitable, an exception is thrown during the build.

## Configuration

In order to configure a daemon to be able to act as a build cluster, specify the [`-cluster-enable`](/doc/guide/cmdlineref/daemon_start.md#-cluster-enable) command line flag in addition when starting it. (You may need to set [`-server`](/doc/guide/cmdlineref/daemon_start.md#-server) as well.) To use a build cluster you need to [`-connect`](/doc/guide/cmdlineref/build.md#-connect) to it and use the [`-cluster`](/doc/guide/cmdlineref/build.md#-cluster) argument to specify that it should be used as one.

Adding a build cluster to your build is simple as the following:

```
-connect <address> mycluster
-cluster mycluster
```

For further configuration options see [](cmdlineref/index.md).

## Recommendations

We recommend following the advices below when using build clusters.

### Mirror directory

A key thing for running build clusters is having a mirror directory available. [Mirror directories](pathconfiguration.md#mirror-directory) are places in the file system where tasks temporarily place working copies of files. Tasks which are cluster dispatchable often invoke external processes that require the files to be present somewhere in the file system.

Mirror directories are necessary for making this possible, therefore we strongly recommend setting a cluster mirror directory for the associated daemon. (See [`-cluster-mirror-directory`](/doc/guide/cmdlineref/daemon_start.md#-cluster-mirror-directory) daemon argument.)

### Build environment

The environemnts of the build clusters should be as similar as possible. You should make effort to have the same SDKs and other development tools installed on each of the clusters. The tasks that support build clusters may decide that a given cluster is not suitable for its invocation if it has different SDKs installed on it that is incompatible with the build.