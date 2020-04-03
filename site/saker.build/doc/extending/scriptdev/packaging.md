# Packaging

Your scripting language implementation should be distributed in a JAR file. The produced JAR should be self contained, meaning that **all** of the runtime classes should be bundled in it. It should not rely on manifest header like `Class-Path` and others. The build system will not take those into account when loading your classes.

Your package should contain a class file which is an implementation of `ScriptAccessProvider`. This class will be loaded first when the script language implementation is used.

Your archive may be [multi-release](https://openjdk.java.net/jeps/238), the build system will load the classes accordingly. However, module related information on JDK 9+ may not be taken into account. 

The above is the bare minimum that you need to package in your archive, the language will be useable with the build system in this way. However, in order to do that, the user will need to specify the exact name of the class, as in the following command line argument example:

```
-script-files **/*.lang 
-script-classpath pwd://language.jar 
-script-class example.lang.LanguageAccessProvider
```

The above example sets an example language to be used for files with the extension `.lang`, and loaded from the `language.jar` that is in the current process working directory. The language provider will be the class with the name `example.lang.LanguageAccessProvider`.

Note that the `-script-class` option was necessary for the build system to locate the entry point for your language. In order to avoid this, you can declare the `saker.build.scripting.ScriptAccessProvider` service in your JAR, using the [`ServiceLoader`](https://docs.oracle.com/javase/8/docs/api/java/util/ServiceLoader.html) facility of the Java runtime.\
This way, the `-script-class` option may be omitted.

<small>

Although declaring the service can be convenient, users should be noted that the `ServiceLoader` can find other classes too which are not present in the distributed JAR. This can happen in various cases such as when the user specifies a class path for the Java process that already has some script language service declaration. In order to promote determinism and reproducability, users are recommended to use the `-script-class` option.

</small>