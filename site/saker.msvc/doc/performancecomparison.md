# Performance comparison

In this article we'll be comparing the performance of the saker.msvc package to other tools that are the most appropriate canditate for a given use-case.<noscript> (Enabling JavaScript will convert the below measurement tables to bar graphs.)</noscript>

The goal of these comparisons is **not** to measure the compilation speed of [`cl.exe`](https://docs.microsoft.com/en-us/cpp/build/reference/compiling-a-c-cpp-program?view=vs-2019) and other MSVC tools. The goal of the comparisons is to measure the scheduling performance of the saker.msvc build tasks and compare them to common tools used in the same ecosystem. We are interested in the performance results of the following use-cases:

* Compiler process creation and management
	* We want to ensure that the saker.msvc build tasks can spawn and manage the compilation processes that are invoked as part of the build in an optimal manner.
* Build task distribution to clusters
	* We are interested in the amount of overhead build clusters add to the build execution.
* Incremental performance
	* We want to measure how fast can the build systems determine what needs to build and what is up-to-date.

In the following tests we'll be using the [build daemon](root:/saker.build/doc/guide/daemon.html) to keep cached information about the compiled project. We'll be running the tests on Window 10, with the Windows Defender turned off. We'll use the [`Measure-Command`](https://docs.microsoft.com/en-us/powershell/module/microsoft.powershell.utility/measure-command?view=powershell-6) PowerShell cmdlet to measure the time it takes to execute a build.

We'll also include relevant build times of the saker.build tool if it is invoked inside an IDE. In these cases the startup cost of the client JVM that initiates the build can be omitted. It generally adds 500-600 ms to the measured times, and we feel that this is an important aspect and more relevant in everyday use-cases.

## Ninja

[Ninja](https://ninja-build.org/) is a widely used tool that is used in the C/C++ ecosystem. Most commonly it is a backend for other meta build systems that run the processes as part of the build. It focuses on speed, and is an appropriate candidate to compare the performance of saker.build.

In the following tests we generated 1000 source files each with a single declared function. No include directories and special options are passed to the `cl.exe` compiler. We don't perform linking of the created object files. Important aspect is that both saker.build and Ninja executes the same command line commands.

The project is available [here](https://github.com/sakerbuild/performance-comparisons/tree/bf6bbd6f2d9bb0eb66eeb1973dc069bf5c0a2ba7/msvc-ninja-1000).

<div id="perf-1000-ninja" style="--doc-metric:'ms';" class="doc-bar-graph" doc-label-y="duration">

<div class="doc-bar-graph-title">Ninja comparison</div>


| Test case                         	| Ninja 	| Saker.build IDE 	| Saker.build 	|
|-----------------------------------	|-------:	|-----------------:	|-------------:	|
| Clean build                       	| 9422  	| 10209           	| 10716       	|
| No-op (no changes)                	| 14    	| 39              	| 538         	|
| Incremental single change         	| 46    	| 86              	| 587         	|
| Incremental every 3rd file change 	| 2958  	| 3511            	| 4014        	|

</div>

In the above tests we've performed clean builds, no-op incremental builds when we modify no source files, incremental builds when only a single file was modified, and incremental builds when every 3rd source file was modified. 

From the results we can see that saker.build is 8% slower for clean builds, and 18% slower when performing incremental builds than Ninja when it comes to invoking the compilation processes. If it is acceptable to you, that's for you to decide. However, we feel that when compared to a specialized native tool, the above results are great. Let's see why we say this:

* The saker.build runs in the JVM. It is actually expected to be somewhat slower than a native tool.
	* It actually would've been much more slower, as the [`ProcessBuilder`](https://docs.oracle.com/javase/8/docs/api/java/lang/ProcessBuilder.html) class that can be used to create external processes adds a significant overhead for creating processes. We've implemented our own native process creation mechanism which reduces the time it takes to start a process. Without it, the clean builds would've taken around 15-16 seconds for this test case.
* The example uses very small and simple source files. As your codebase gets larger, and the source files grow, the time it takes to manage the compilation processes will diminish in comparison to the actual time running `cl.exe` takes.

## Build cluster scheduling

Saker.msvc supports dispatching compilation tasks to clusters. In the following measurements we'll be comparing the scheduling performance of the saker.msvc compilation build task to the [IncrediBuild](https://www.incredibuild.com/) tool.

We will be using the same test case as in the [](#ninja) comparison.

We use a second PC that is attached as an agent/cluster to our main one. The second PC is slower than the coordinator. It usually takes 23 seconds to compile the test case using Ninja. Based on this, we assume that the agent has only the 41% performance compared to the coordinator. Based on this, the optimal scenario in which both PCs work on 100% during the compilation, the build would take around 7.25 seconds (without any network communication).

IncrediBuild is run inside Visual Studio 2019. The linking times are not included in the results. The CPUs total in 17.6 GHz, and 6 cores. We're using the verison 9.4.4 (build 3042).

<div id="perf-1000-cluster" style="--doc-metric:'ms';" class="doc-bar-graph" doc-label-y="duration">

<div class="doc-bar-graph-title">Distributed compilation scheduling</div>

| Test case                         	| Saker.build 	| IncrediBuild 	|
|-----------------------------------	|------------:	|-------------:	|
| Clean build                       	|        8969 	|        66000 	|
| No-op (no-changes)                	|          40 	|         4550 	|
| Incremental single change         	|          94 	|         5550 	|
| Incremental every 3rd file change 	|        3141 	|        29800 	|

</div>

Well, the results are not what we expected *at all*. Although IncrediBuild says on their site they reduce build times by 90%, it seems they increased them by 90%. We think this may be due to the following:

* At the start of every build, IncrediBuild spends 4-6 seconds *Preparing build tasks*. For incremental builds, this is more than the entire compilation of the changed sources take.
* The operations surrounding the source files far outweight the time it takes to compile a file. Any checks that need to be performed when compiling a file may take longer than the time it takes to compile it. These times may add up.
* Maybe we didn't set up the PCs properly. We just downloaded IncrediBuild, installed on the coordinator and agent, and run the builds. We haven't configured anything. 

But what about our results compared to the [](#ninja) measurements? Our clean and incremental build times decreased by **11-12%**. The presence of build clusters didn't decrease the build times, which is important as unnecessary network communications could have a negative impact.

Based on the results, we conclude that for this case, using build clusters and distributed compilation is unnecessary, as the minor improvements doesn't worth the hassle. In this test case the network communication between agents may have a significant impact on the build times, and therefore not providing a noticable benefit. This test is still important in one way as it shows that using clusters won't impact the performance negatively for such edge case.

## Build cluster realistic example

In the previous test case we've seen that the network communication can have a non-negligible impact on the distributed compilation, if the compilation times are minimal. In the following test, we'll be looking on a more realistic example. The example will have source files that take longer to compile, and use include directories that need to be synchronized with the build clusters.

The test case contains of 1000 C++ source files with each of them including `<Windows.h>`, `<vector>`, and random amount of user includes that are also part of the project. The inclusion of these files cause the compilation times to go up (around 600-700 ms on our main machine), therefore the the compilation times should now outweigh the network communication times.

The project codebase is available [here](https://github.com/sakerbuild/performance-comparisons/tree/bf6bbd6f2d9bb0eb66eeb1973dc069bf5c0a2ba7/msvc-cluster-realistic-1000).

<div id="perf-realistic-1000-cluster" style="--doc-metric:'ms';" class="doc-bar-graph" doc-label-y="duration">

<div class="doc-bar-graph-title">Realistic distributed compilation</div>

| Test case                         	| Saker.build + cluster 	| Saker.build 	| IncrediBuild 	|
|-----------------------------------	|----------------------:	|------------:	|-------------:	|
| Clean build                       	|                149321 	|      189956 	|       213000 	|
| No-op (no-changes)                	|                    41 	|          46 	|         5025 	|
| Incremental single change         	|                   803 	|         854 	|         7600 	|
| Incremental every 3rd file change 	|                 51313 	|       63782 	|        75333 	|

</div>

The above includes results for compiling the sources with the attached build cluster, and we also included the build times for saker.build that only uses the local machine. The results are more realistic than the previous one, however, saker.build is still **30%** faster than IncrediBuild when it comes to distributing the build tasks.

Adding the build cluster to the compilation resulted in a **20%** performance increase for both clean and incremental builds when using saker.build. The cluster that we added to the build performs clean builds in 663 seconds. (It is the same PC that was used in the previous test, however, the workload is different.) We can determine that the attached cluster has only the 28% of the performance than the coordinator.

Based on the above, the optimal scenario where both the coordinator and the cluster works at 100% capacity, the build would take about 147 seconds. The actual measured clean build result is **149 seconds**, which is only ~1.5% off the optimal scenario. Given that there are many aspects that play a role in the performance measurements, we consider this a great success for implementing an efficient distribution mechanism.

## Conclusion

We've that using the saker.msvc package to compile your C/C++ sources is a comparable solution to other existing tools when it comes to build times. Although the saker.build system has minimal overhead, the measurements show that it doesn't impact the build times in a way that distrupts developer workflow.

The measurement results for distributed compilation show that saker.build can efficiently scale to multiple machines and provides optimal work distribution to the build clusters. When compared to IncrediBuild, it can perform distributed compilation **30%** faster in a realistic build scenario.

## Notes from the author

If I was reading these results, I'd be very sceptic based on them. The distributed compilation results are not what I'd expect when comparing to a tool that is on the market since 2002. IncrediBuild claims on their site that they accelerate build times up to 90%, and they are even [endorsed by Microsoft](https://devblogs.microsoft.com/visualstudio/improving-your-build-times-with-incredibuild-and-visual-studio-2015/). It comes as a surprise that they perform so poorly in the above test cases so we can't do anything else than feel that maybe we haven't configured something properly. The fact that using it actually increases the build times really makes us wonder.

So in light of the above, if anybody from the IncrediBuild team stumbles upon these results, please feel free to contact us so we can fix the results. (See the associated [GitHub issue](https://github.com/sakerbuild/saker.msvc/issues/1))

[!include](buildres:/inc/bargraph.inc.txt)