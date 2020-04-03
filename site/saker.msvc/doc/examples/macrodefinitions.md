# Macro definitions

The [`saker.msvc.ccompile()`](/taskdoc/saker.msvc.ccompile.html) task allows you to add macro preprocessor definitions to the compiled files. This corresponds to the [`/D`](https://docs.microsoft.com/en-us/cpp/build/reference/d-preprocessor-definitions?view=vs-2019) parameter of [`cl.exe`](https://docs.microsoft.com/en-us/cpp/build/reference/compiling-a-c-cpp-program?view=vs-2019):


```sakerscript
saker.msvc.ccompile([
	{
		Files: src/**/*.cpp,
		MacroDefinitions: {
			MY_MACRO: 123
		}
	},
	{
		Files: src/**/*.c,
		MacroDefinitions: {
			MY_MACRO: 456
		}
	}
])
```

In the above all C++ files will have the `MY_MACRO` with the value `123` defined for them, while for C files the `MY_MACRO` will have the `456` value.

To define a macro without any value, use the following:

```sakerscript
saker.msvc.ccompile(
	{
		Files: src/**/*.cpp,
		MacroDefinitions: {
			MY_MACRO: ""
		}
	}
)
```

This way the actual argument passed to the [`cl.exe`](https://docs.microsoft.com/en-us/cpp/build/reference/compiling-a-c-cpp-program?view=vs-2019) is `/DMY_MACRO`.
