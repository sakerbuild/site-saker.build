# Control flow

The execution of the declared expressions are ordered differently than you'd expect based on other languages. They are not executed in the order of their declaration, but based on their input dependencies.

The build system operates with a high level of concurrency. Basically, it runs everything that it can in a parallel manner. In order to accomodate that, the build language needs to be able to evaluate as many expressions as possible at once. The build system also specifies that the tasks should be pure functions, meaning that they shouldn't have any visible side effects to other tasks.

This results in an execution model that eagerly runs the declared expressions and schedules them accordingly if they access their input data. The following example provides an example for this:

```sakerscript
$compiledSources = example.compile.sources()
example.export.project($compiledSources)
```

The above example compiles some sources of the project, and assigns the compilation result to the `$compiledSources` variable. The `example.export.project()` task takes the compilation results and packages them in the requested way. The build system will execute these in proper order, as the two tasks are connected by the variable that conveys the information.

If we combine the above with the `foreach` loop, we can have a clearer example for the advantage of parallelization:

```sakerscript
foreach $arch in [x64, x86, arm] 
	with $compiledSources {
	$compiledSources = example.compile.sources(Architecture: $arch)
	example.package.project($compiledSources)
}
```

We have three target architectures that we want to compile our projects for. The `foreach` loop declares the compilation and packaging tasks for each architecture. (The `$compiledSources` variable is local to the `foreach` loop.)

The build system can easily parallelize this, and improve the performance of the build significantly, as the compilation tasks for each architectire are independent. This means that for every architecture, the compilation and packaging will run separately and in parallel to other architecture tasks.

In general, the build language specifies that all expressions in the script are evaluated independently, and concurrently. The order of their declaration doesn't matter. Please read [](/doc/scripting/executionmodel.md) for more information.
