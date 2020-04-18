# Performance comparison

In this article, we'll be showing the results of performance measurements including Java testing. We'll be comparing the testing performance of the saker.java.testing package against the [Gradle](https://gradle.org/) build tool.<noscript> (Enabling JavaScript will convert the below measurement tables to bar graphs.)</noscript>

If you're intereseted in Java compilation performance, see [here](root:/saker.java.compiler/doc/performancecomparison.html).

All the comparisons are done on the same and single machine. The comparions are done on Windows 10, with the Windows Defender being turned off all times. When doing the comparisons, we'll be using the build daemons of each participating tool when applicable.

During the measurements, we'll be doing some warm-up round(s) for each tools which won't be part of the result. Then we compute the average of the measured times and that will be the result of a measurement. We don't use any benchmarking/profiling tools, and don't set any options that instrument the tools for benchmarking.

We'll use the [`Measure-Command`](https://docs.microsoft.com/en-us/powershell/module/microsoft.powershell.utility/measure-command?view=powershell-6) PowerShell cmdlet to measure the time it takes to execute a build. (We use the time the cmdlet displays instead that the given tool displays to have a consistent measuring practice.) Note that when the tools are used inside an IDE, the startup cost of the Java processes may be omitted.

When clean builds are measured, the previous outputs are manually cleaned from the output directory rather than running `clean` commands for a given tool.

## Large monolithic project

We measure the testing performance on the [single-large-project](https://github.com/gradle/performance-comparisons/tree/13739fa299e485c079335b0cd5b30da1cff92234/single-large-project) that is published by Gradle specifically for performance comparisons. It contains 50000 source files and 50000 associated test cases. 

We've modified the project a little bit, in order to make the comparison fairer. We disabled report generation for Gradle, and increased the number of JVM forks for testing to match the settings we use for saker.build. The following was set for the `build.gradle` file:

```groovy
test {
    reports {
        junitXml.enabled = false
		html.enabled = false
    }
	maxParallelForks = 4
}
```

Let's see the results:

<div id="perf-large-monolithic-test" style="--doc-metric:' ms';" class="doc-bar-graph" doc-label-y="duration">

<div class="doc-bar-graph-title">Large monolithic project compile + test</div>

| Test case                        	| Saker.build 	| Gradle 6.0.1 	|
|----------------------------------	|------------:	|-------------:	|
| Clean compile + test             	|      108245 	|       193096 	|
| No-op (no-changes)               	|         762 	|         5799 	|
| Incremental test class change    	|        2934 	|        36384 	|
| Incremental source class change 	|        3298 	|        35097 	|

</div>

Based on the results we can see that clean compilation and testing is performed **44%** by saker.build. In case of incremental changes, the performance increase is around **90%**! We can clearly see an advantage of the [`saker.java.test()`](/taskdoc/saker.java.test.html) task in that it tracks the dependencies of each invoked test case. In the incremental scenarios, it only needed to reinvoke the modified test case instead of invoking all 50000 of them.

<div class="doc-wip">

This article is work in progress. In the future we'd like to add comparisons for other, more realistic projects. We'd also like to employ some more performance optimizations in the Java testing build task, and also compare the testing performance if there are no dependency instrumentation for the test cases.

</div>

[!include](buildres:/inc/bargraph.inc.txt)