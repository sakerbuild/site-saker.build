# Using switch expressions, text blocks, and `var` on Java&nbsp;8

<small>2020 May 18</small>

If you've been following the news about the Java language, you probably know that among others, [switch expressions](https://openjdk.java.net/jeps/361), [text blocks](https://openjdk.java.net/jeps/355), and [local type inference](https://openjdk.java.net/jeps/286) has been introduced to the language. If you're anything like us, you probably haven't even tried them out due to the fact that our code still has to run on Java 8. Let's change that!

As an update to the saker.build system, you can now configure the Java compilation in a way that lets you use (some of) the new language features while still being able to run on Java 8.

The examples for the post is available on GitHub: [java-target-older-releases](https://github.com/Sipkab/java-target-older-releases).

## Feature overview

Some of the notable language features that were added since Java 8 are the following:

<small>(Skip this if you're already familiar with them.)</small>

#### Switch expressions

[Switch expressions](https://openjdk.java.net/jeps/361) for Java was standardized in Java 14. It basically allows you to use `switch` in more places as well as returning values from them:

```java
// from https://openjdk.java.net/jeps/361
switch (day) {
    case MONDAY, FRIDAY, SUNDAY -> System.out.println(6);
    case TUESDAY                -> System.out.println(7);
    case THURSDAY, SATURDAY     -> System.out.println(8);
    case WEDNESDAY              -> System.out.println(9);
}
T result = switch (arg) {
    case L1 -> e1;
    case L2 -> e2;
    default -> e3;
};
```

#### Text blocks

[Text blocks](https://openjdk.java.net/jeps/355) let you declare string literals that consist of multiple lines without dealing with concatenation and escaping:

```java
// from https://openjdk.java.net/jeps/355
engine.eval("function hello() {\n" +
            "    print('\"Hello, world\"');\n" +
            "}\n" +
            "\n" +
            "hello();\n");
// becomes
engine.eval("""
	        function hello() {
	            print('"Hello, world"');
	        }
	        
	        hello();
	        """);
```

#### Local type inference

[Local type inference](https://openjdk.java.net/jeps/286) was introduced in Java 10 and allows you to declare local variables with the `var` keyword. The compiler will deduce the type of the variable for you:

```java
// from https://openjdk.java.net/jeps/286
var list = new ArrayList<String>();  // infers ArrayList<String>
var stream = list.stream();          // infers Stream<String>
```

## Using new language features

To use these features while targetting Java 8, you need to set the release, and source version parameters for the Java compiler.
When using `javac` directly (or via other means), it won't allow you to do this and report an error for mismatching configuration.

However, with the [`saker.java.compile()`](root:/saker.java.compiler/taskdoc/saker.java.compile.html) build task for saker.build you can circumvent this restriction:

```sakerscript
saker.java.compile(
	SourceDirectories: src,
	Parameters: [
		--enable-preview,
		--release, 8
	],
	PatchEnablePreview: true,
	AllowTargetReleaseMismatch: true,
	SourceVersion: 14,
	# No need to specify TargetVersion as the --release parameter implies it
	#TargetVersion: 8,
	SDKs: {
		# Use JDK 14 for compilation
		Java: saker.java.sdk(14),
	}
)
```

Breaking it down:

* `--enable-preview` lets you use the preview features of the used Java compiler. For Java 14 it enables us to use text blocks and pattern matching.
* `--release 8` specifies that the boot classpath for Java 8 should be used when compiling the sources. This will prevent possible binary incompatibilities as referencing missing APIs will be reported as compilation errors.
* [`PatchEnablePreview`](root:/saker.java.compiler/taskdoc/saker.java.compile.html#PatchEnablePreview) lets us run the Java classes without specifying `--enable-preview` for the `java` command.
* [`AllowTargetReleaseMismatch`](root:/saker.java.compiler/taskdoc/saker.java.compile.html#AllowTargetReleaseMismatch) is a safeguard parameter to allow mismatching target and release configuration. If it is not set to `true`, then the usual error will be reported for invalid configuration.
* [`SourceVersion`](root:/saker.java.compiler/taskdoc/saker.java.compile.html#SourceVersion) sets the source version that our Java files should conform to.

**And basically that's it!** \
You can try it out by cloning, building, and running the [example repository](https://github.com/Sipkab/java-target-older-releases). \
View the [example source file](https://github.com/Sipkab/java-target-older-releases/blob/master/src/test/Main.java) to see other language features that are also compatible. \
View the compilation and test results on [Azure Pipelines](https://dev.azure.com/sipkab/java-target-older-releases/_build/results?buildId=53&view=logs&j=12f1170f-54f2-53f3-20dd-22fc7dff55f9&t=59a85588-b0ba-5043-24c4-d9e29d89c6f6).

Or read on for more details.

## How does it work?

We don't do anything magic. Javac will actually happily generate the class files for older release versions without us diving deep into the internal details of javac. There's no source rewriting or other shenanigans going on.

The trick is not allow validation of the source version, target version, and `--release` configuration before invoking javac itself. This can be done by invoking javac through Java code rather than using the command line.

You may see the following warning emitted: 

```plaintext code-wrap
jrt:/jdk.scripting.nashorn/module-info.class: Warning: Cannot find annotation method 'forRemoval()' in type 'java.lang.Deprecated'
```

This can happen when you specify `--release 8` and is nothing to be afraid of. It is a side effect of how the classes are structured and analyzed when compiling for other releases. The `forRemoval()` method was added to `@Deprecated` in Java 9, but the compiler can't find it in Java 8. This warning can be safely ignored.

## Limitations

You are limited to using new language features that don't introduce new bytecode. This entails that if you target Java 8, then you can't use [modules](https://openjdk.java.net/jeps/261) or [records](https://openjdk.java.net/jeps/359).

You can use the new language features that essentially boil down to just being syntactic sugar. The ones mentioned in this article are notable examples of these. You can view the [example source file](https://github.com/Sipkab/java-target-older-releases/blob/master/src/test/Main.java) to see other but less prevalent ones. (pattern matching, generic subclass, private interface methods)

### Pitfalls

In some cases you may encounter compilation errors. One such is when you set the target version higher than the `--release` version.

If we add 9 as the target version in the above example:

```sakerscript
saker.java.compile(
	Parameters: [
		--enable-preview,
		--release, 8
	],
	SourceVersion: 14,
	TargetVersion: 9,
)
```

Then the compilation will simply throw a fatal error:

```
com.sun.tools.javac.util.FatalError: Fatal Error: Unable to find method makeConcatWithConstants
	at com.sun.tools.javac.comp.Resolve.resolveInternalMethod(Resolve.java:2763)
	at com.sun.tools.javac.jvm.StringConcat$IndyConstants.doCall(StringConcat.java:490)
	at com.sun.tools.javac.jvm.StringConcat$IndyConstants.emit(StringConcat.java:450)
	at com.sun.tools.javac.jvm.StringConcat$Indy.makeConcat(StringConcat.java:275)
	at com.sun.tools.javac.jvm.Gen.visitBinary(Gen.java:2122)
```

This is because javac will attempt to generate code that is expected to run on Java 9. [JEP 280: Indify String Concatenation](https://openjdk.java.net/jeps/280) introduced a new way of generating string concatenation code and it requires APIs that aren't available on Java 8, therefore an error is generated.

To avoid this, **make sure the target version is not higher than the `--release` version**. This is usually the case when compiling Java, so generally you shouldn't encounter this error.

## Binary compatibility

The method we present in this post doesn't use a well documented API of javac. It is more like a *hack* that causes javac to behave in a way that we want, but was not designed for. \
*Questions may arise whether or not this is a future-proof and compatible way of using new language features?*

**The answer is no**. Future Java releases may break this way of using new language features on older releases. Although it's not expected, you may still encounter binary incompatibility. We recommend thorough testing of your code before deploying it to production.
