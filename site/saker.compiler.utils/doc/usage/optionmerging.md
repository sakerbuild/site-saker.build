# Compiler option merging

Some tasks may support the operation of *compiler option merging*. It is a feature that dynamically merges other compiler options into the specified configuration.

Option merging usually consists of the following:

* A compilation task [compilation identifier](compilationid.md).
* Explicit configuration on the compilation task.
* A specified collection of compiler options.
	* Compiler options may or may not have individual compiler identifiers assigned to them.
* Other qualifier properties based on the associated task.

Option merging is usually used to specify the compiler options in a different and maintainable way than directly on the task that is being invoked. A simple and great example for this is by specifying different linker options based on the identifier:

(Take from the [saker.msvc](root:/saker.msvc/doc/ccompile/linkeroptions.html) package.)

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

In the above we use the [MSVC C/C++ compiler toolchain](https://docs.microsoft.com/en-us/cpp/build/reference/c-cpp-building-reference?view=vs-2019) to link some inputs. We specify the `LinkerOptions` parameter that consists of a list of additional compiler options.

We can see that one linker task has the `main-debug` and the other has the `main-release` compilation identifiers assigned to them. The [`saker.msvc.clink()`](root:/saker.msvc/taskdoc/saker.msvc.clink.html) task will take this, and attempt to merge the specified `LinkerOptions` if applicable.

It does the examination by checking if the compilation identifier specified in an option map is the [subset](compilationid.md#subset) of the identifier of the linker task. If so, then it will merge the specified options when linking the inputs. In the end, this will cause that the input specified for `main-debug` will be linked with the [`/DEBUG`](https://docs.microsoft.com/en-us/cpp/build/reference/debug-generate-debug-info?view=vs-2019) flag, and the inputs for `main-release` are linked with the [`/LTCG`](https://docs.microsoft.com/en-us/cpp/build/reference/ltcg-link-time-code-generation?view=vs-2019) flag passed to the backend linker.

This is a great use-case for compilation identifiers, as it makes compiler options reusable for multiple compilation tasks without having the need for the developer to repeat themselves every time. Following up on the above example, adding the given linker flags can be achieved by simply appending `-release` or `-debug` to the compilation identifier of a linker task.

**Note** that the actual rules for option merging may be specialized for a given use-case. For example, the above [`saker.msvc.clink()`](root:/saker.msvc/taskdoc/saker.msvc.clink.html) task will also take the `Architecture` of the task and compiler options into account.
