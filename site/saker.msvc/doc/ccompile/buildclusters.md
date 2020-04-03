# Build clusters

The [`saker.msvc.ccompile()`](/taskdoc/saker.msvc.ccompile.html) and [`saker.msvc.clink()`](/taskdoc/saker.msvc.clink.html) tasks support distributing their workloads to build clusters. This improves concurrency and performance of builds, as more machines can be included in executing the compilations.

The task distribution to the build clusters are **automatic**. The tasks do this by evaluating where the compilations and linking can be executed, and the workload is automatically distributed. No other user-side configuration is required by the tasks.

The determination whether or not a given cluster is available for workload distribution is based on whether or not the [specified SDKs](sdkmanagement.md) are available on it or not. If all the SDKs which are specified for a given task are the same on a cluster machine, then it will be used for compilation or linking.

Note that the semantically same SDKs need to be installed on the clusters for the tasks to be able to execute on them. In order to encourage deterministic builds, different SDK installations cannot be used to compile sources in the same compilation pass.

The tasks allow that a given SDK is not required to be installed on the local machine. It is possible to execute compilation using build clusters even if you don't have it installed on the build executor machine. E.g. you can compile using MSVC if you run the build on a macOS machine if you have at least one build cluster that has the toolchains installed.

Note that while the linking operation can be distributed to clusters, only one machine will be used to link the given inputs as its workload cannot be separated into multiple processes.
