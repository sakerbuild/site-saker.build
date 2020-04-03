# Script languages

The build system runs its builds based on the contents of build scripts. The build scripts define what tasks should be executed during the build. Saker.build includes its custom [build language](/doc/scripting/index.md) that can be used to describe your build process. However, it also provides an API for creating your own languages, therefore declaring your builds in a different way than the default.

By default, the SakerScript language will be used to parse any build script that has the extension `.build` (case sensitive).

Other scripting languages may be loaded by the build system using JAR (Java ARchive) files. The archives should be a self contained bundle of class files and other resources that implement the required features for integrating with the build system. See [](/doc/extending/scriptdev/index.md).

The script language parsers may be configured with arbitrary key-value string options. These options may be interpreted by the language parser in order to modify the behaviour of the language parser. For the options intepreted by each language parser see the documentation for the associated language.

The built-in SakerScript currently defines no parsing options.

## Configuration

During a build execution only a single language can be associated with a given build script file. The languages can be configured using wildcards that will be tested if they match the given script file path. This allows a per-file based configuration.

By default, the SakerScript language is defined for all files that match the pattern `**/*.build` that is all files which have the case sensitive extension of `.build`.

If multiple languages are defined, the first language of the configuration will be used that has a matching wildcard pattern. 

For defining other languages see the [command line reference](cmdlineref/build.md).

### Examples

In order to override the default `.build` extension for the SakerScript language, use the following command line arguments:

```
-script-files **/*.myextension
```

---

To load a custom language from a JAR distribution:

```
-script-files **/*.lang 
-script-classpath pwd://language.jar 
-script-class example.lang.LanguageAccessProvider
```

The above example sets an example language to be used for files with the extension `.lang`, and loaded from the `language.jar` that is in the current process working directory. The language provider will be the class with the name `example.lang.LanguageAccessProvider`.

Note that in this case the built-in language will **not** be used, and `.build` files will not be parsed using SakerScript. In order to use that append the `-script-files **/*.build` options as well.

---

To define multiple languages for the build execution and some parser options in addition:

```
-script-files **/*.lang 
-script-classpath pwd://language.jar 
-script-class example.lang.LanguageAccessProvider
-script-files **/*.secondlang 
-script-classpath pwd://secondlanguage.jar 
-script-class example.secondlang.LanguageAccessProvider
-SOmy.option=123
-SOmy.second.option=456
-SOmy.flag.option
```

The configuration of a language use is started with the `-script-files` argument, and other following arguments will modify the last configuration that was started. The [`-SO`](/doc/guide/cmdlineref/build.md#-so) argument is used to define the parser options in `<key>=<value>` format. The `=<value>` part may be omitted to specify flags.
