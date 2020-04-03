# Precompiled headers

The [`saker.msvc.ccompile()`](/taskdoc/saker.msvc.ccompile.html) task supports using precompiled headers during compilation. Precompiled headers can improve build times by precompiling infrequently changing header files and reusing them later. See more information on the [Precompiled Header Files | Microsoft Docs](https://docs.microsoft.com/en-us/cpp/build/creating-precompiled-header-files?view=vs-2019) site for an in-depth explanation.

In order to use precompiled headers, you need to specify the `PrecompiledHeader` parameter for the source files in your build task invocation:

```sakerscript
saker.msvc.ccompile({
	Files: src/*.cpp,
	PrecompiledHeader: pch/pch.h,
})
```

The above will result in the `pch/pch.h` header being precompiled before any of the `cpp` files, and it can be used in the source files themselves. The precompiled header can be included in your source files with the `#include "pch.h"` line as the first line of the source code. The expected for the `#include` statement is the same as the file name of the precompiled header:

```cpp
#include "pch.h"

int main() {
	return 0;
}
```  

If you have multiple source files, the precompiled header will be compiled only once by the build task and reused for inclusion in multiple source files. It is recommended that frequently used header files are added to the precompiled header, as this can improve compilation times.

The build task also supports using precompiled headers on multiple machines. That is, if you use build clusters to distribute the compilation tasks, the precompiled headers are used on the clusters as well. (See [](#notes) for more information.)

## Force inclusion

As you've seen, in order to use the precompiled header, you need to include it in the first statement of the compiled source files. If you don't include it, the compiler will throw an error.

As it may feel like a chore, you can specify the [`ForceIncludePrecompiledHeader`](/taskdoc/types/CompilationInputPassTaskOption.html#f-ForceIncludePrecompiledHeader) parameter to have that done automatically for your. If you set it to `true`, the build task will automatically add an appropriate [`/FI`](https://docs.microsoft.com/en-us/cpp/build/reference/fi-name-forced-include-file?view=vs-2019) command line argument to force include the precompiled header.

**Note** that we don't recommend using this as it may cause portability issues to other build system.

## Notes

Using precompiled headers may differ from the experience you're used to in Visual Studio.

First of all, you don't need a separate `cpp` or other compilation unit file for your precompiled header. The task will compile the precompiled header file as is. This means that the actual input to the compiler is the `pch.h` instead of constructing a new compilation unit. (I.e. you don't need `stdafx.cpp` or other files.)

When using build clusters and precompiled headers, you should know that the precompiled header objects will not be shared between build machines. The precompilation will be done on all build clusters that are asked to execute a compilation. This is because the compiler limits the usage of the precompiled header objects to a single machine, and they cannot be reliably shared between build machines. See [`/Yu`](https://docs.microsoft.com/en-us/cpp/build/reference/yu-use-precompiled-header-file?view=vs-2019) option.

The precompiled header object files are reused between builds if their inputs haven't changed. This caching is also performed on the build clusters, so you generally don't need to worry about the precompilation being done on all clusters. Incremental builds won't suffer from this drawback.

If you [force include](forceinclude.md) other files in your compilation, those inclusions will be present **before** the precompiled header. The force included files will be part of the precompiled header.
