# Passing compiler/linker parameters

Sometimes you may want to directly pass some simple parameters to the backend [`cl.exe`](https://docs.microsoft.com/en-us/cpp/build/reference/compiling-a-c-cpp-program?view=vs-2019) or [`link.exe`](https://docs.microsoft.com/en-us/cpp/build/reference/linking?view=vs-2019). You can do that using the `SimpleParameters` parameter/property:

```sakerscript
$ccompile = saker.msvc.ccompile(
	{
		Files: src/**/*.cpp,
		SimpleParameters: [ /O2 ]
	},
)
saker.msvc.clink(
	$ccompile,
	SimpleParameters: [ /LTCG ],
)
```

In the above we pass the [`/O2`](https://docs.microsoft.com/en-us/cpp/build/reference/o1-o2-minimize-size-maximize-speed?view=vs-2019) argument for the compiler backed, and the [`/LTCG`](https://docs.microsoft.com/en-us/cpp/build/reference/ltcg-link-time-code-generation?view=vs-2019) for the linker. This is in order to enable some optimizations for the compiled and linked code.

Note that you can only pass **simple** parameters to the compiler/linker backend. Simple parameters are the ones that does not have an argument and can be used on its own. The tasks may de-duplicate the specified simple parameters, and may reorder them.

*You shouldn't specify files and other complex parameters for this option.* Use the task input parameters/properties to specify them. (E.g. Use `IncludeDirectories` for specifying include directories instead of adding a [`/I`](https://docs.microsoft.com/en-us/cpp/build/reference/i-additional-include-directories?view=vs-2019) parameter.)

If a parameter that you want to add is not simple, but has no corresponding task input parameter/property, then it is not currently supported by the task implementation. We're adding more and more functionality as the saker.msvc package is being developed.
