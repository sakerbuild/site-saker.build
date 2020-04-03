# Foreach loop

The `foreach` loop in the build language can be used to iterate over elements of a list or map. Its main purpose is to allow executing the same operations for multiple configurations.

```sakerscript
foreach $arch in [x64, x86] : [ 
	example.compile.sources(SourceDirectory: src, Architecture: $arch) 
]
```

In the above example, we iterate over a list of architecture identifiers and invoke the compilation for each given architecture. It can also be used to build a project with different flavors:

```sakerscript
foreach $lang in [en, fr, de] : [ 
	example.compile.sources(Sources: [src, "lang-{ $lang }"]) 
]
```

The above example takes a list of languages, and compiles the project with the sources from the `src` directory, and the sources from the `lang-<language>` directory. This results in the project compiled for each specified language.

The above examples are simplified, and serve as a basic introduction. We recommend reading [](/doc/scripting/langref/foreach/index.md) for more information.
