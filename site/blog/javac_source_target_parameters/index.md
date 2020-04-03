# Beware of the `-source` and `-target` javac parameters

<small>2020 February 27</small>

If you've been cross-compiling Java code for older releases, and were using the `-source` and `-target` javac parameters, you may experience unexpected errors when your app is deployed. Let's see why.

In the following, we'll compile a simple class to Java 8, using javac from JDK 9. The source is as follows:

```java code-block-pad
import java.nio.ByteBuffer;

public class Compile8Test {
    public static void main(String[] args) {
        ByteBuffer bb = ByteBuffer.allocate(16);
        bb.flip();
        System.out.println("Success");
    }
}
```

It's quite simple and should properly work on Java 8, 9, or later. 

### Using `--release`

Let's compile it for Java 8, using the `--release` option.

```plaintext code-block-pad
path/to/jdk9/bin/javac Compile8Test.java -release 8
```

As a test, we should run it on **Java 8**:

```plaintext code-block-pad
path/to/java8/bin/java Compile8Test
```

The output is nothing out of the ordinary:

```plaintext code-block-pad
Success
```

This was the correct example, showcasing how you *should* cross-compile Java for older versions.

### Using `-source` and `-target`

The `--release` option was introduced in JDK 9. If you're like us, you may've just upgraded your Java version without putting much thought into how it may affect your compilation process. You (or your underlying build system) may've kept using the `-source` and `-target` parameters although the `--release` was just introduced.

Let's see how it can affect our compilation output:

```plaintext code-block-pad
path/to/jdk9/bin/javac Compile8Test.java -source 8 -target 8
```

It compiles fine, we do receive a warning though:

```plaintext code-block-pad
warning: [options] bootstrap class path not set in conjunction with -source 1.8
```

Whatever... Warnings are just warnings. Let's run it!

```plaintext code-block-pad
path/to/java8/bin/java Compile8Test
```

The output it not what we'd expect:

```plaintext code-block-pad
Exception in thread "main" java.lang.NoSuchMethodError: java.nio.ByteBuffer.flip()Ljava/nio/ByteBuffer;
        at Compile8Test.main(Compile8Test.java:6)
```

Okay, so what's up? Well the fact is that although we specified the source and target levels to conform to Java 8, the compilation is still done against the classes in JDK 9! Issues don't surface often because of this, as Java is generally developed in a backward compatible way. However, a new method was introduced in `ByteBuffer` in JDK 9:

```java code-block-pad
@Override
public ByteBuffer flip() {
    super.flip();
    return this;
}
```

It's basically just for convenience to return a more suitable type of the buffer from the method. As it was introduced in Java 9, running the compiled code on Java 8 will fail, as the code still references the above method, but it's not present in Java 8. (Well, it is present as it is overridden, but it has a different return type, therefore the method lookup based on the descriptor fails.)

We can see this more in detail if we run `javap`:

```plaintext code-block-pad
> path/to/java8/bin/javap -v -c Compile8Test.class
public class Compile8Test
  minor version: 0
  major version: 52
  flags: (0x0021) ACC_PUBLIC, ACC_SUPER
[ ... ]
  public static void main(java.lang.String[]);
    descriptor: ([Ljava/lang/String;)V
    flags: (0x0009) ACC_PUBLIC, ACC_STATIC
    Code:
      stack=2, locals=2, args_size=1
         0: bipush        16
         2: invokestatic  #2  // Method java/nio/ByteBuffer.allocate:(I)Ljava/nio/ByteBuffer;
         5: astore_1
         6: aload_1
         7: invokevirtual #3  // Method java/nio/ByteBuffer.flip:()Ljava/nio/ByteBuffer;
        10: pop
        11: getstatic     #4  // Field java/lang/System.out:Ljava/io/PrintStream;
        14: ldc           #5  // String Success
        16: invokevirtual #6  // Method java/io/PrintStream.println:(Ljava/lang/String;)V
        19: return
```

See the line with `7: // Method java/nio/ByteBuffer.flip:()Ljava/nio/ByteBuffer;`? That method doesn't exist on Java 8! To put that in contrast with the `javap` output of the correctly cross-compiled version:

```plaintext code-block-pad
  public static void main(java.lang.String[]);
    descriptor: ([Ljava/lang/String;)V
    flags: (0x0009) ACC_PUBLIC, ACC_STATIC
    Code:
      stack=2, locals=2, args_size=1
         0: bipush        16
         2: invokestatic  #2  // Method java/nio/ByteBuffer.allocate:(I)Ljava/nio/ByteBuffer;
         5: astore_1
         6: aload_1
         7: invokevirtual #3  // Method java/nio/ByteBuffer.flip:()Ljava/nio/Buffer;
      [ ... ]
```

You can see that the descriptor of the called methods are different.

## Mitigation

The straightforward solution for the above problem is to just use the `--release` parameter. However, if you use JDK 8 to compile, that is not possible. In most cases you don't even interact with `javac` directly, but use some build system that calls it for you. In any cases, you will need a build system that can properly handle the `--release` flag if running on JDK 9+, or use the `-source` and `-target` parameters for older releases.

Another solution is to use javac that comes with the JDK you're targetting. You may also need to set the `-bootclasspath` for the compilation if targetting Java 7 or older releases. In all cases however, you need to be careful with your compilation setup when doing cross-compilation.

You may be happy to know that saker.build supports [cross-compilation](root:/saker.java.compiler/doc/javacompile/crosscompile.html) in various ways that mitigate this problem.

## Conclusion

We've seen that using the `-source` and `-target` parameters when cross-compiling Java may cause unexpected issues in your deployment environment. If you may take one advice from us is that you should check your build tools if they perform this correctly. This issue probably doesn't affect you, as these are edge cases, and your integration tests probably discovered them already. In any case, we hope this post was informational to you.

<small>

This post was made because we've discovered an issue with the `apksigner` tool when developing [Android support](https://github.com/sakerbuild/saker.android) for saker.build. It was mistakenly compiled for Java 9 in the 30 rc1 release of the build tools, and as we load the `apksigner.jar` directly in the build JVM process, it caused errors such as above.

We filed an [issue](https://issuetracker.google.com/issues/150189789) in the respective tracker, and hope it will be resolved soon. In any way, thankfully this doesn't block the development of Android support for saker.build, as this issue can be mitigated easily by instrumenting the loaded classes and replacing the affected method calls.

</small>
