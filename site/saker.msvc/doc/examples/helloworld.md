# Hello world

This example shows a minimal example for creating a simple console application using the MSVC toolchain:

```sakerscript
$ccompile = saker.msvc.ccompile(
	src/**/*.cpp,
	CompilerOptions: saker.msvc.coptions.preset(console)
)
saker.msvc.clink(
	$ccompile,
	LinkerOptions: saker.msvc.coptions.preset(console)
)
```

In the above we have a source file in the `src/main.cpp` path:

```cpp
#include <stdio.h>
int main() {
	printf("Hello world!");
	return 0;
}
```

Compiling and linking the above example will result in an executable in the build directory at the path: `saker.msvc.clink\default\x64\default.exe`.

Running it, it will print `Hello world!` and exit.

Adding the `console` preset configuration as in `saker.msvc.coptions.preset(console)` is needed, because we need to access the `stdio.h` header file, and linking the program requires the libraries available from the Windows SDK. The `console` preset appropriately sets them up.
