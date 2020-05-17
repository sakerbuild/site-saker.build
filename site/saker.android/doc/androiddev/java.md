# Java compilation

The main language in which you can write your Android app is Java\*. The compilation will take the source files you've written, and the `R.java` class as its input.

To compile Java, the [`saker.java.compile()`](root:/saker.java.compiler/taskdoc/saker.java.compile.html) task can be used.

```sakerscript
$aapt2 = saker.android.aapt2.link(### ... ###)
$javac = saker.java.compile(
	SourceDirectories: [ src ] + $aapt2[JavaSourceDirectories],
	BootClassPath: saker.android.classpath.platform(),
	SDKs: {
		AndroidPlatform: saker.android.sdk.platform(),
		AndroidBuildTools: saker.android.sdk.buildtools(),
	}
)
```

In the above, we compile all the `.java` sources from the `src` directory, and pass the output Java source directory from the `aapt2` linker task. That directory contains the generated `R` class that can be used to reference the resources of your application.

The `BootClassPath` is set to be the classes that comes with the Android platform. This allows you to use classes such as [`android.app.Activity`](https://developer.android.com/reference/android/app/Activity) and others.

The `SDKs` argument contains the necessary [SDK references](sdkmanagement.md) that comes with the Android SDK. This lets the Java compiler task know where to look for the input files for Android Java compilation.

We're not done yet! In order to be able to run our code on Android devices, we need to convert the compiled classes to Dex Bytecode. Visit the [](dexing.md) page for more information.

<small>\* Kotlin is also a popular programming language for creating Android apps, but saker.build doesn't support it yet.</small>
