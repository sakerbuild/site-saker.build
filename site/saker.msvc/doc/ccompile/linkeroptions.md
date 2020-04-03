# Linker options

The [`saker.msvc.clink()`](/taskdoc/saker.msvc.clink.html) task allows dynamic option merging based on the identifier and architecture of the linker operation. It can be used to define the linker options in one place that can be applied to one or more linker operations selectively.

A simple example for this is to define different linker parameters for `debug` and `release` builds:

```sakerscript
$options = [
	{
		Identifier: debug,
		SimpleLinkerParameters: [ /DEBUG ]
	},
	{
		Identifier: release,
		SimpleLinkerParameters: [ /LTCG ]
	},
]
saker.msvc.clink(
	Input: ### ... ###,
	Identifier: main-debug,
	LinkerOptions: $options
)
saker.msvc.clink(
	Input: ### ... ###,
	Identifier: main-release,
	LinkerOptions: $options
)
```

The above will cause the [`/DEBUG`](https://docs.microsoft.com/en-us/cpp/build/reference/debug-generate-debug-info?view=vs-2019) argument to be passed to the [`link.exe`](https://docs.microsoft.com/en-us/cpp/build/reference/linking?view=vs-2019) backend for the `main-debug` linker operation, while the [`/LTCG`](https://docs.microsoft.com/en-us/cpp/build/reference/ltcg-link-time-code-generation?view=vs-2019) argument to be passed for the `main-release` linker operation.

A linker option entry will be merged with the linker operation if all of the dot separated identifier parts are contained in the operation identifier of the task.

The mergeability of the options can also be decided based on the target `Architecture` of the linked inputs.
