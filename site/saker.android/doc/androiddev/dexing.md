# Dexing

When developing Android applications, programmes usually compile their sources to Java bytecode. However, Android devices can only interpret Dex bytecode, so we need to perform conversion between the two.

Dexing is the operation that performs this by taking the input Java bytecode classes and creating the Dex files which can be added to the Android application bundle.

Performing dexing on the compiled Java classes is as simple as:

```sakerscript
$javac = saker.java.compile(### ... ###)
$d8 = saker.android.d8($javac)
```

The [`saker.android.d8()`](/taskdoc/saker.android.d8.html) task takes the Java compilation output and generates the appropriate Dex files for the application. It will (transitively) include all input classpath for the compilation.

## Multidex 

You may encounter build errors in cases where there are too many references are used in your application code. Dex files have a limitation on the max number of references used in them (64K), so you need to use multidexing if you encounter this limitation.

Multidexing means that your application code will be split up into multiple `.dex` files so the 64K limit will not be a problem. When using multidexing, you should keep the following in mind:

In an Android APK, there is a main dex file, and optionally additional dex files. The main dex file is named `classes.dex` while any additional dex files will be named `classesN.dex` where N is greater or equals to 2. If you're minimum SDK version is 21 (Android 5.0) or greater then then the Android OS natively supports multidex applications.

However, if you're developing for Android API level 21 or less, then you need to programmatically ensure that the additional dex files are loaded. 

In order to enable generating multiple dex files, you need to pass at least one main dex class to the [`saker.android.d8()`](/taskdoc/saker.android.d8.html) task. These specified classes will be part of the main dex file. It usually includes the class name of your Application.

```sakerscript
saker.android.d8(
	$javac
	MainDexClasses: [
		com.example.MyApplication
	]
)
```

You can view the [Android Developers documentation](https://developer.android.com/studio/build/multidex) on multidexing, but make sure to adapt the build system specific information for saker.build.
