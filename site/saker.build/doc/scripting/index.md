# Overview

The build system comes with a custom built-in language for writing build scripts. It was designed to allow the description of the software build process in a way that combines both imperative and declarative features. Based on that, SakerScript is an eager data-driven language for the saker.build system.

The build system also provides an API for creating your own scripting language. See [](/doc/extending/scriptdev/index.md) for more information.

## Features

SakerScript was designed to excel in functionality that allows easier definition of your build process rather than dealing with the quirks of a language. Some of its features are highlighted here.

### Data-driven control flow

What's the most readable description of a build process? Simple statements in the order of their execution.<sup>\*</sup>

What's the most performant description of a build process? Declarative definition of build tasks and their dependencies.<sup>\*</sup>

SakerScript combines both. The statements in the build script can be declared in any order while maintaining the same execution flow of the build. The expressions are evaluated eagerly and in parallel based on the operands of theirs. A language like this is readable, as you can exactly trace back the source of a value while being performant, as the interpreter can evaluate as many expressions as possible at once.

```sakerscript
$archive = example.download.archive("http://example.com/myarchive.jar")
example.extract.archive($archive, Directory: output)
```

In the above example, we download some archive, and the extract its contents to the specified `output` directory. It is straightforward, however, if you change the order:

```sakerscript
example.extract.archive($archive, Directory: output)
$archive = example.download.archive("http://example.com/myarchive.jar")
```

Exactly the same things will happen. The order of statement declarations are irrelevant. The extraction of the downloaded archive will only happen once the download has finished. This way the build system can appropriately parallelize the build tasks as the inputs of the tasks define the order of execution not their declaration order.

However, for the sake of readability, we recommend following the order of the first example.

<small>

\* The answers are based on empirical data.

</small>

### It's a configuration language

SakerScript was designed to configure the components for your build process, not to write low-level build steps in it. This means that the language doesn't define constructs such as conditional loops, and the variables in it doesn't have any state. Immutability of the data is key for providing the control flow of the language.

### Clutter-free syntax

Most languages require you to use characters that doesn't give much additional meaning to the described data. In SakerScript, quotes around literals, semicolons, are optional, extra commas won't result in syntax errors. This can improve readability of the code as you don't have to deal with the unnecessary visual noise.

SakerScript also defines only the minimal necessity of language expressions which are required for a complete build process definition. This results in fewer obscure features of the language and aims to avoid confusions about lesser known expressions of the language when new developers are getting started with it.

### Extendable

The language relies on extensions to provide usable functionality. It can be extended by creating custom task implementations that the build language can invoke. By using it with the saker.build tool, these extensions are provided by the build system and its own plugin mechanism.

### Rich IDE support

The language implementation provides rich features for IDE usage. It provides content assistant to accelerate writing scripts, semantic highlight of the source code elements, documentation lookup for task extensions, and code outline for quicker navigation.

The content assistant and documentation feature are also extensible by the build system plugins. Any custom language made for the build system can take advantage of these features. 

### Typeless

Types can be a great feature of a language, but they also introduce significant complexity to it. SakerScript's main focus is on configuration and defining types only add unnecessary maintenance burden to it rather than real functionality. SakerScript allows declaring complex data structures via lists and maps, but intentionally lacks the ability to define types.
