# Feature comparison

On the [overview page](index.md) we've claimed that the saker.java.compiler package provides the *single, correct and only complete solution for incremental Java compilation that is currently available in the software development industry*. This document makes an attempt to prove that by comparing the feature sets of existing compilation techniques with saker.java.compiler.

The premise of the comparison that the solutions must support incremental Java compilation in some way. Incremental compilation is the act of reusing previous outputs to produce compilation result, and execute only the minimal amount of work required. Incremental compilation can provide a significant build speed boost when large or multi-module projects are in play.

Based on the above, we've found that the existing solutions for incremental Java compilation are the following:

* The incremental Java compiler available for the Eclipse IDE. (via [JDT Core Component](https://www.eclipse.org/jdt/core/)) 
* The Gradle [Java plugin](https://docs.gradle.org/current/userguide/java_plugin.html) that supports incremental Java compilation. (Introduced in [Gradle 3.4](https://blog.gradle.org/incremental-compiler-avoidance))

*Disclaimer: If we've missed any existing solutions, or are misinformed with the mentioned technologies in the article, please [contact us](raw://contact.html) so we can correct ourselves.*

## Eclipse Java Compiler

The Java compiler used in the Eclipse IDE is a complete reimplementation of the Java compiler. It doesn't require a JDK, and has no relation to the `javac` tool. This has both advantages and disadvantages. It induces an enormous maintenance burden on the authors, as they need to keep up with the compiler development as the Java language evolves. 

The separate implementation may also contain bugs that otherwise aren't present in the `javac` implementation. Some examples that we've found during the development of the saker.java.compiler package (and haven't been fixed yet):

* [Java compilation succeeds when functions collide by type variable](https://bugs.eclipse.org/bugs/show_bug.cgi?id=531202)
* [Compilation fails for heavily templated functional interface](https://bugs.eclipse.org/bugs/show_bug.cgi?id=528859)
* [Eclipse compiler resolves typename from superclass instead of template type](https://bugs.eclipse.org/bugs/show_bug.cgi?id=528363)

These bugs however, doesn't reduce the great impact that it has on the Java ecosystem. The Eclipse Java Compiler can be very well used in the Eclipse IDE as it can compile the modified sources nearly instantaneously.

However, when annotation processors are used, the Eclipse Java Compiler falls short. It doesn't provide any incremental compilation related features for annotation processors, which can result it being tedious or simply unsuitable to use. We consider this a serious shortcoming, as the annotation processors can greatly improve the Java development experience. 

One of the most popular annotation processors, [google/dagger](https://github.com/google/dagger) has 14.5k starts on GitHub, signaling that annotation processors shouldn't be neglected in relation with Java compilation. 

The Eclipse Java Compiler works very well in the IDE, however, in cases when it needs to be integrated with build systems, and build processes that aren't directly related to the Eclipse IDE, we haven't found a way to have incremental compilation features. Meaning that as far as we know, the Eclipse provided incremental Java compilation features only available inside an IDE, and not externally invokeable. (Please correct us if we're wrong.)

## Gradle Java plugin

The Gradle build system comes with a Java plugin that supports incremental Java compilation. They implement this by analyzing the bytecode that comes out of `javac`, and determine the dependencies for the compiled classes. They also support incremental annotation processing which is not a straightforward thing to implement.

We can say that they are pioneers in regards of improving the Java compilation experience for the Java ecosystem, and their efforts are not to be overlooked. However, their solution has major shortcomings, and not because of the flaws in the implementation, but because the Java class file format doesn't provide enough information to properly determine the dependencies based on them.

Some of the problems that arise with their solution in the importance order we see fit:

**Constant declarations cause full recompilation.** ([Related issue](https://github.com/gradle/gradle/issues/6482)) Java constants are field declarations that are `public static final`, have a primitive or `String` type, and is initialized with a constant expression. Like the followings:

```java
public static final int MAGIC_NUMBER = 123;
public static final String SECRET_PASSWORD = "...";
public static final int SOME_FLAG_VALUE = 1 << 12;
```

If you modify a Java source file that contains public constants, then Gradle will do a full recompilation of the project. This is unacceptable, and can seriously disrupt workflow for large projects. (See their own [performance comparison project](https://github.com/gradle/performance-comparisons/tree/master/single-large-project) that contains 50 000 source files. Compilation can even take 50 seconds.)

The official solution for this is to not use constants in your codebase. We believe that the tooling should never impose restrictions on the codebase of your project, and this falls into that scenario.

**Dead code elimination can leave sources in an erroneous state.** `javac` will not include instructions in the resulting class files if it can determine that the given code will never be executed. This dead code elimination can cause the codebase to contain unnoticed errors even after a successful compilation. Let's look at the following code (with some boilerplate context eliminated for compactness):

```java
// Foo.java:
public static final boolean SOME_CONDITION = false;

// Baz.java:
if (Foo.SOME_CONDITION) {
	Bar.doSomething();
}

// Bar.java:
public static void doSomething() {
	// ...	
}
```

We compile the code, and it succeeds. The call to `Bar.doSomething()` in `Baz` will be eliminated by the Java compiler. Everything's fine, however, we later decide to modify *only* the `Bar.doSomething()` method signature to include a new parameter:

```java
// Bar.java:
public static void doSomething(int value) {
	// ...	
}
```

If we recompile our project, it succeeds, although **it should not**. It should fail, as the `Bar.doSomething()` in `Baz` is now invalid. However, due to the fact that the class file of `Baz` doesn't contain a reference to `Bar.doSomething()`, `Baz` will **not** be recompiled.

So we've succeeded with the compilation, the tests, and we push our code to the source repository. The first place where we're noticed about the error is the CI server that we've hopefully set up for our project. We believe that this is an unacceptable scenario. You might think that this is an insignificant edge-case, however, there are many use-cases like testing and build flags, flavors, where issues like this can often emerge. 

**Template parameter modification may result in incorrect code.** If a type is only present as a template arugment for a type, Gradle may not recompile the affected class in case of class hieararchy modifications. If we start out with the following sources:

```java
// Foo.java:
public class Foo { ... }

// Baz.java:
// (local variable)
ArrayList<Foo> list = new ArrayList<>();
```

The compilation will succeed. If we the decide to completely delete the `Foo` class, Gradle may not recompile `Baz.java`, therefore leaving it in an erroneous state. This is a minor edge-case that we believe rarely happens, but still shows limitations of the class file based dependency discovery.

There are other smaller edge cases that may leave the codebase in an erroneous state, or straight up produce incorrect code, however, they are so rarely occurring that we won't bore the reader with it. Most of them are related to wildcard imports, [package level class additions](https://github.com/gradle/gradle/issues/8590), or specific naming edge cases that are all limitations of the class file format. Instead let's focus on how saker.java.compiler improves this.

## Saker.java.compiler

Our goal when implementing the [`saker.java.compile()`](/taskdoc/saker.java.compile.html) task was to improve the incremental Java compilation performance as much as we can while providing correct outputs. We've implemented a solution purely based on the [Java Compiler API](https://docs.oracle.com/javase/8/docs/jdk/api/javac/tree/index.html) that supports correct dependency discovery of every compiled class and allows us to implement incremental annotation processing. 

In our implementation we run the annotation processing ourselves, which has the disadvantage of having to reimplement the mechanism provided by `javac`, however, the advantage of supporting source retention annotations and parallel annotation processing greatly outshine that drawback.

Our implementation still has some missing features, but that is only because the lack of time spent implementing them, and not due to other technical limitations. One such restriction is that class file generation is not yet supported for annotation processors. Some rarely used features are also not yet available using the [`saker.java.compile()`](/taskdoc/saker.java.compile.html) task.

In the [performance measuresments](performancecomparison.md) we also prove that our implementation is the best solution for fast Java compilation.

## Conclusion

We've seen that existing solutions for correct and fast incremental Java compilation have lacking support for core features of Java development. With the [`saker.java.compile()`](/taskdoc/saker.java.compile.html) task we present an alternative that compiles Java sources in a correct and scalable way, both for large and complex projects.
