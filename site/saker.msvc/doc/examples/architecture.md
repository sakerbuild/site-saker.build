# Architecture

The [`saker.msvc.ccompile()`](/taskdoc/saker.msvc.ccompile.html) and [`saker.msvc.clink()`](/taskdoc/saker.msvc.clink.html) tasks allow you to specify a target architecture for which the operations should be done. The target architecture determines what kind of architecture the sources should be compiled and then linked for.

You can specify the target architecture using the `Architecture` parameter:

```sakerscript
$ccompile = saker.msvc.ccompile(
	src/**/*.cpp,
	Architecture: x86
)
saker.msvc.clink($ccompile)
```

In the above example we specify the target architecture `x86` for which the sources should be compiled for. We don't specify the architecture for the linker task, as it will be inferred from the output of the compilation task.

Note that you may still specify an architecture for the linker task. If not specified, it will attempt to infer it based on the inputs. If it fails to infer, the target architecture will be the same as the current build execution machine.

Note that it may happen to misconfigure the architectures, or mistakenly mix them up:

```sakerscript
$ccompile = saker.msvc.ccompile(
	src/**/*.cpp,
	Architecture: x86
)
saker.msvc.clink(
	$ccompile,
	Architecture: x64
)
```

The above will most likely not work, and the backend [`link.exe`](https://docs.microsoft.com/en-us/cpp/build/reference/linking?view=vs-2019) may throw an error or appropriate warnings signaling the misconfiguration. Note that the task implementations doesn't necessarily check the matching architectures themselves, as they don't perform deep analysis of the linker input files.

The supported architectures are `x86`, `x64`, or in some cases `arm`, `arm64`. (Supporting ARM may require installing additional tooling.)