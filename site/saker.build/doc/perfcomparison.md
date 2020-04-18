# Performance comparisons

Saker.build is fast. But don't take our word for it, take our graphs for it!<noscript> (Enabling JavaScript will convert the below measurement tables to bar graphs.)</noscript>

We'll be showcasing the performance measurements from the [saker.java.compiler](root:/saker.java.compiler/doc/performancecomparison.html) and the [saker.msvc](root:/saker.msvc/doc/performancecomparison.html) packages.

## Java compilation

We've made many efforts to improve both or clean and incremental Java compilation times. As a result, saker.build compiles Java projects **15-70%** faster compared to [Gradle](https://gradle.org/). For clean builds, the following results were measured:

<div id="perf-clean-java-compile" style="--doc-metric:' ms';" class="doc-bar-graph" doc-label-y="duration">

<div class="doc-bar-graph-title">Clean Java compilation</div>

| Test case                	| Saker.build 	| Gradle 	|
|--------------------------	|------------:	|-------:	|
| Apache commons library   	|        2227 	|   2698 	|
| Large multi-project      	|       20133 	|  36774 	|
| Large monolithic project 	|       31258 	|  62419 	|

</div>

When it comes to clean build performance, saker.build overtakes Gradle in every of the above cases. The difference for smaller projects (Apache commons) may be smaller, but when it comes to large codebases, the advantage is closer to **40-50%**.

However, incremental build times matter more when it comes to the edit-build-run development cycle:

<div id="perf-incremental-java-compile" style="--doc-metric:' ms';" class="doc-bar-graph" doc-label-y="duration">

<div class="doc-bar-graph-title">Incremental Java compilation</div>

| Test case                      	| Saker.build 	| Gradle 	|
|--------------------------------	|------------:	|-------:	|
| Apache commons library         	|         793 	|   1641 	|
| Large multi-project ABI keeping  	|        1190 	|   3541 	|
| Large multi-project ABI changing 	|        4062 	|   6979 	|
| Large monolithic project       	|        1287 	|   5879 	|

</div>

No matter how you structure your codebase, the incremental compilation implementation for Java determines the sources to recompile in a fast and correct way. Saker.build is faster in all cases, providing a quicker feedback for your developers.

## C/C++

Saker.build is a general build system, therefore supporting any kind of language for which the build tasks are implemented. We've added support for C/C++ compilation using the MSVC toolchain. As a starter, we measured the performance of how saker.build can manage the scheduling of the compilation tasks. For this, we compared the results to the [Ninja](https://ninja-build.org/) build tool. 

<div id="perf-cpp-ninja" style="--doc-metric:' ms';" class="doc-bar-graph" doc-label-y="duration">

<div class="doc-bar-graph-title">C/C++ builds</div>

| Test case                         	| Ninja 	| Saker.build 	|
|-----------------------------------	|------:	|------------:	|
| Clean build                       	|  9422 	|       10209 	|
| Incremental single change         	|    46 	|          86 	|
| Incremental every 3rd file change 	|  2958 	|        3511 	|

</div>

We can see that saker.build provides similar performance to Ninja. Some overhead is introduced as saker.build runs on the JVM, while Ninja is a native tool specialized on this use-case. Saker.build performs 8-18% slower in the above cases, however, we believe that the configureability and versatility of saker.build makes up for it.

## Build clusters

Saker.build supports build clusters which can be used to spread out the build task workloads to different build machines. This feature is relevant when large codebases need to be built and the build tasks are highly parallelizable.

Building C/C++ sources are such a task, as the source files that need to be compiled can be passed to worker machines that perform the compilation. Utilizing multiple machines to execute the build can significantly shorten the time it takes.

For cluster comparison we measured the performance of saker.build against the [IncrediBuild](https://www.incredibuild.com/) tool. The test case distributes 1000 source files and uses 2 worker machines to compile C++ sources.

<div id="perf-cluster-incredibuild" style="--doc-metric:' ms';" class="doc-bar-graph" doc-label-y="duration">

<div class="doc-bar-graph-title">Distributed compilation</div>

| Test case                         	| Saker.build 	| IncrediBuild 	|
|-----------------------------------	|------------:	|-------------:	|
| Clean build                       	|      149321 	|       213000 	|
| Incremental single change         	|         803 	|         7600 	|
| Incremental every 3rd file change 	|       51313 	|        75333 	|

</div>

Saker.build performs both clean and incremental builds **30%** faster than IncrediBuild when build clusters are in play.

## Further reading

The above performance measurements have been take from the packages that are used to implement the associated build tasks. See the following articles about each comparions and how they've been conducted:


* [Saker.java.compiler performance comparisons](root:/saker.java.compiler/doc/performancecomparison.html)
* [Saker.msvc performance comparisons](root:/saker.msvc/doc/performancecomparison.html) 

[!include](buildres:/inc/bargraph.inc.txt)