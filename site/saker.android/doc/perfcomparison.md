# Performance comparison

In the following, we'd like to present some of the build performance comparisons that we've made when building Android applications. The measurements will be performed for saker.build as well as for [Gradle](https://gradle.org), the official build system for Android.<noscript> (Enabling JavaScript will convert the below measurement tables to bar graphs.)</noscript>

All the comparisons are done on the same and single machine. The comparions are done on Windows 10, with the Windows Defender being turned off all times. When doing the comparisons, we'll be using the build daemons for both tools when applicable.

We'll perform 8 runs for a given case and take the lowest (fastest) time result for each tool. This lowest measurement will be the result for a given test case.

We'll use the [`Measure-Command`](https://docs.microsoft.com/en-us/powershell/module/microsoft.powershell.utility/measure-command?view=powershell-6) PowerShell cmdlet to measure the time it takes to execute a build. (We use the time the cmdlet displays instead that the given tool displays to have a consistent measuring practice.) Note that when the tools are used inside an IDE, the startup cost of the Java processes may be omitted.

When clean builds are measured, the previous outputs are manually cleaned from the output directory rather than running `clean` commands for a given tool.

The Gradle wrapper wasn't used for running Gradle, it was invoked directly from the installation directory.

## Medium sized project

For this test case we've constructed a medium sized Android application that has:

* 3000 Java source files
* 500 layout resource files
* 500 drawable resource files
* 115 PNG image files
* 1000 string declarations in a single resource XML 

The test case is available [on GitHub](https://github.com/sakerbuild/performance-comparisons/tree/78cd8d7896c4b0255ec77304762471e6cab95411/android-medium). The measurements are performed for building the debug APK for the Android application. It includes compilation of the Android resources, the associated Java code, converting it to dex format, and finally creating and signing the APK.

The results of our measurements are the following:

<div id="perf-android" style="--doc-metric:' ms';" class="doc-bar-graph" doc-label-y="duration">

<div class="doc-bar-graph-title">Android build time comparison</div>

| Test case               	| Saker.build 	| Gradle 6.3|
|-------------------------	|------------:	|-------:	|
| Clean build             	|       11396 	|  15836 	|
| Java incremental        	|        2656 	|   3596 	|
| Modify Android resource 	|        2778 	|   3519 	|
| Add Android resource    	|        4224 	|   5081 	|
| No-op (no changes)       	|         614 	|   2214 	|

</div>

We can see that saker.build performed the builds **15 - 28%** faster, averaging around **22%** (not taking no-op into account).

Clean builds are mostly self explanatory, while for incremental builds we've done the following:

* **Java incremental**: We modified a string value (literal) inside the method body of [`MainActivity.onCreate()`](https://github.com/sakerbuild/performance-comparisons/blob/78cd8d7896c4b0255ec77304762471e6cab95411/android-medium/app/src/main/java/saker/android/perftest/MainActivity.java#L16).
* **Modify Android resource**: Modified the value of a string resource in [`strings.xml`](https://github.com/sakerbuild/performance-comparisons/blob/78cd8d7896c4b0255ec77304762471e6cab95411/android-medium/app/src/main/res/values/strings.xml).
* **Add Android resource**: Added a new string resource to [`strings.xml`](https://github.com/sakerbuild/performance-comparisons/blob/78cd8d7896c4b0255ec77304762471e6cab95411/android-medium/app/src/main/res/values/strings.xml).

We believe that these kind of modifications are often done when developing Android applications and reflect the build times one 
should expect when building the apps.

<div class="doc-wip">

This article is work in progress. In the future we'd like to add comparisons for more use-cases and for different project sizes. Android support is still in early stages for saker.build, so as more functionality gets added (such as Kotlin and NDK support), we'll update the this article accordingly.

</div>

[!include](buildres:/inc/bargraph.inc.txt)