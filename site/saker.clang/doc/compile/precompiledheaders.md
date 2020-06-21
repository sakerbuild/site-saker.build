# Precompiled headers

The [`saker.clang.compile()`](/taskdoc/saker.clang.compile.html) task supports using precompiled headers during compilation. Precompiled headers can improve build times by precompiling infrequently changing header files and reusing them later. 

In order to use precompiled headers, specify the `PrecompiledHeader` parameter for the inputs in your build task invocation:

```sakerscript
saker.clang.compile({
	Files: src/*.cpp,
	PrecompiledHeader: pch/pch.h,
})
```

The above will result in the `pch/pch.h` header being precompiled before any of the `cpp` files, and it will be automatically included before the source code for the `cpp` files. You don't need to explicitly `#include` the precompiled header file.

If you have multiple source files, the precompiled header will be compiled only once by the build task and reused for inclusion in multiple source files. It is recommended that frequently used header files are added to the precompiled header, as this can improve compilation times.

The build task also supports using precompiled headers on multiple machines. That is, if you use build clusters to distribute the compilation tasks, the precompiled headers are used on the clusters as well.
