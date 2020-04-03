# Hello DLL

This example shows a minimal example for creating a simple Dynamic Link Library using the MSVC toolchain:

```sakerscript
$ccompile = saker.msvc.ccompile(
	src/**/*.cpp,
	CompilerOptions: saker.msvc.coptions.preset(dll)
)
saker.msvc.clink(
	$ccompile,
	LinkerOptions: saker.msvc.coptions.preset(dll)
)
```

In the above we have a source file in the `src/main.cpp` path:

```cpp
#include <stdio.h>
extern "C" __declspec(dllexport) int my_function() {
	printf("Hello DLL!");
	return 0;
}
```

The example only differs from [](helloworld.md) in that we use the `dll` preset rather than `console`.

Compiling and linking the above example will result in a library in the build directory at the path: `saker.msvc.clink\default\x64\default.dll`.

Adding the `dll` preset configuration as in `saker.msvc.coptions.preset(dll)` is needed, because we need to access the `stdio.h` header file, and linking the program requires the libraries available from the Windows SDK. The preset also adds the [`/DLL`](https://docs.microsoft.com/en-us/cpp/build/reference/dll-build-a-dll?view=vs-2019) linker argument to create a DLL. The `dll` preset appropriately sets them up.

You can also load the created DLL and use the exported function with the following code:

```cpp
#include <Windows.h>

typedef int (* my_function_t)();

HMODULE h = LoadLibrary("path\\to\\.dll");
my_function_t proc = (my_function_t) GetProcAddress(h, "my_function");
proc();
CloseHandle(h);
```