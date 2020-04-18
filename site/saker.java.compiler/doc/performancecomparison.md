# Performance comparison

In the following article we'd like to present some performance comparions of our [Java compilation task](/taskdoc/saker.java.compile.html) compared to other existing solutions.<noscript> (Enabling JavaScript will convert the below measurement tables to bar graphs.)</noscript>

All the comparisons are done on the same and single machine. The comparions are done on Windows 10, with the Windows Defender being turned off all times. When doing the comparisons, we'll be using the build daemons of each participating tool when applicable.

During the measurements, we'll be doing some warm-up round(s) for each tools which won't be part of the result. Then we compute the average of the measured times and that will be the result of a measurement. We don't use any benchmarking/profiling tools, and don't set any options that instrument the tools for benchmarking.

We'll use the [`Measure-Command`](https://docs.microsoft.com/en-us/powershell/module/microsoft.powershell.utility/measure-command?view=powershell-6) PowerShell cmdlet to measure the time it takes to execute a build. (We use the time the cmdlet displays instead that the given tool displays to have a consistent measuring practice.) Note that when the tools are used inside an IDE, the startup cost of the Java processes may be omitted.

When clean builds are measured, the previous outputs are manually cleaned from the output directory rather than running `clean` commands for a given tool.

The following comparisons will focus plainly on clean and incremental Java compilation. If you're interested in Java testing performance comparison, have a look [here](root:/saker.java.testing/doc/performancecomparison.html).

## Gradle comparison

In the following measurements we'll be focusing on the [Gradle](https://gradle.org/) build tool. It is popular in the JVM ecosystem, and also supports other languages. We won't be directly comparing to [Maven](https://maven.apache.org/), as one can deduce the results based on our and the [Gradle vs Maven performance comparison](https://gradle.org/gradle-vs-maven-performance/).

In our tests we'll be performing the same test-case scenarios that the Gradle team used vs Maven. We'll also add some other comparisons that include annotation processing and other use-cases.

We'll be using the (currently latest) `6.0.1` version of Gradle in the following tests.

### Apache Commons Lang

For this measurement we take the [current latest](https://github.com/apache/commons-lang/tree/404d67841ecba0d2a5e8055fe5650907d582ac03) available version of the master branch, and perform clean and incremental compilations.

<div id="perf-apache-commons" style="--doc-metric:' ms';" class="doc-bar-graph" doc-label-y="duration">

<div class="doc-bar-graph-title">Apache Commons Lang</div>

| Test case                    	| Saker.build 	| Gradle 6.0.1 		|
|------------------------------	|-------------:	|--------------:	|
| Clean build                  	| 2227        	| 2698         		|
| No-op (no changes)           	| 579         	| 1307         		|
| Incremental NumberUtils.java 	| 793         	| 1641         		|
| Incremental DiffResult.java  	| 731         	| 2693         		|

</div>

We can see that saker.build is faster in all cases. It performs the clean build **17.5%** faster, while the advantage for incremental compilations grows over **50-70%**.

The test case includes two incremental compilation scenarios. In both of them we just simple insert a new line at the start of the file to force its recompilation. The significant difference between them that the `DiffResult` class contains a constant field. In these cases Gradle falls back to full recompilation of the entire Java source set. As we are very fond of constants, we felt that a comparison for this needed to be included.

### Large multi-project

In the following measurement we compile a project that contains of multiple cross-dependent Java subprojects. We took the [Gradle large multi-project](https://github.com/gradle/performance-comparisons/tree/13739fa299e485c079335b0cd5b30da1cff92234/large-multiproject) codebase as the basis of the test, removed unnecesssary testing and library classpath code, and simplified the Gradle build process to have a fair comparison.

We also used the `--parallel` flag when invoking Gradle, as without it the builds were much slower. The actual project that was built is available [here](https://github.com/sakerbuild/performance-comparisons/tree/bf6bbd6f2d9bb0eb66eeb1973dc069bf5c0a2ba7/java-large-multiproject).

<div id="perf-large-multi" style="--doc-metric:' ms';" class="doc-bar-graph" doc-label-y="duration">

<div class="doc-bar-graph-title">Large multi-project build</div>

| Test case                	| Saker.build 	| Gradle 6.0.1 		|
|--------------------------	|-------------:	|--------------:	|
| Clean build              	| 20133       	| 36774        		|
| No-op (no changes)       	| 1040        	| 3541         		|
| Incremental ABI keeping  	| 1190        	| 3541         		|
| Incremental ABI changing 	| 4062        	| 6979         		|

</div>

Saker.build produced **40-70%** faster build times for the above scenarios. When measuring the incremental builds, we separated them into two category. One which doesn't modify the structure of the Java classes (Application Binary Interface keeping), and one that changes the class structure (ABI changing). In both cases the `project1/src/main/java/org/gradle/test/performance1_4/Production1_315.java` file was modified.

When the ABI is kept, we just added a new line at the start of the file to cause the recompilation of the source file. In this case only the modified source file was recompiled, and no other dependent subprojects are compiled again. This is true for both saker.build and Gradle.

For the ABI changing tests, we added a new `public int i` field for every measurement that we've performed. This causes all the subprojects which depend on the structure of the `Production1_315` class to be recompiled. In the end, multiple subprojects are recompiled. This was the case where the difference between the two tools were the smallest, saker.build is only **41%** faster than Gradle.

### Large monolithic project

Next up is a measurement that compiles large amount of Java sources in a single compilation pass. The project is taken from the [Gradle single-large-project](https://github.com/gradle/performance-comparisons/tree/13739fa299e485c079335b0cd5b30da1cff92234/single-large-project) performance comparion scenario, and we measured both tools by running the builds. We've cleaned up the Gradle build file so it doesn't include unnecessary dependencies and IDE plugins.

<div id="perf-large-monolithic" style="--doc-metric:' ms';" class="doc-bar-graph" doc-label-y="duration">

<div class="doc-bar-graph-title">Large monolithic build</div>


| Test case                	| Saker.build 	| Gradle 6.0.1 		|
|--------------------------	|-------------:	|--------------:	|
| Clean build              	| 31258       	| 62419        		|
| No-op (no changes)       	| 611         	| 3329         		|
| Incremental ABI keeping  	| 1287        	| 5879         		|
| Incremental ABI changing 	| 1360        	| 5897         		|

</div>

Although the graph is a bit out of scale due to the huge differences between the clean and incremental build times, we can still see that saker.build is **50-80%** faster in all of the above test cases. The ABI keeping and ABI changing scenarios were done similarly to the [](#large-multi-project) scenario.

### Annotation processing

We'd like to include tests for incremental scenarios using annotation processors, however, we encountered such bugs in the Gradle build system that prevented running them. We wanted to test two annotation processors (one aggregating, one isolating), however, the Gradle builds resulted in compilation errors and stackoverflows.

We'll be running the measurements once the [issue 11559](https://github.com/gradle/gradle/issues/11559) is fixed by Gradle.

## Conclusion

Based on the above measurements we can see that [`saker.java.compile()`](/taskdoc/saker.java.compile.html) task of the [saker.build system](root:/saker.build/index.html) can perform Java builds significantly faster. If we decide to use saker.build inside an IDE, then the incremental compilation times can shrink even more, as the startup time of the JVM can be omitted. This usually results in a decrease of 500-600 ms for the build times.

If the results convinced you, and you feel like saker.build may be an appropriate tool for you use-case, we recommend [trying it out](root:/saker.build/doc/installation.html).

[!include](buildres:/inc/bargraph.inc.txt)