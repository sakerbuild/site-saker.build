# Dependencies

Part of Android development, you'll probably use some external libraries to improve your application. You can use [Android libraries](https://developer.android.com/studio/projects/android-library) which are distributed as AAR files, or JAR files that contain Java classes for your app.

## AAR files

AAR dependencies may contain resources, classes, assets, and other files for your Android app. This needs to be handled appropriately when compiling resources and Java code.

AAR resource compilation needs to be separated into two aapt2 compilation steps:

```sakerscript
$libs = saker.android.aapt2.compile(
	lib/my_dependency.aar,
	Identifier: libs
)
$aapt2 = saker.android.aapt2.compile(res)

$aaptlink = saker.android.aapt2.link(
	$aapt2,
	Overlay: $libs,
	Manifest: AndroidManifest.xml,
	AutoAddOverlay: true
)
```

The above compiles the resources from both the AAR dependency (`lib/my_dependency.aar`) and the usual `res` directory for the project. Both needs to be passed as an input to the linking build task.

The main resources of the application is added as a direct input to the linking phase, while the library dependencies are added as overlay. You set the `AutoAddOverlay` parameter to `true` so the library resources are automatically added to the application.

### AAR classpath

As AAR files may also contain Java classes, we need to add them to the compilation classpath to be properly included in the application:

```sakerscript
$libs = saker.android.aapt2.compile(
	lib/my_dependency.aar,
	Identifier: libs
)
$aaptlink = saker.android.aapt2.link(### ... ###)

$javac = saker.java.compile(
	SourceDirectories: [ src ] + $aaptlink[JavaSourceDirectories],
	ClassPath: saker.android.classpath($libs)
)
```

The above simply creates a classpath configuration based on the compiled AAR libraries. It will extract the necessary embedded JAR files from the AARs and add them to the classpath.

## JAR files

JAR files contain Java classes that are added to the application. They are much simpler to use, as we don't need to deal with resource compilation, only the addition to the classpath:


```sakerscript
$javac = saker.java.compile(
	src,
	Classpath: lib/my_library.jar
)
```

## Maven dependency

Maven can be used to resolve external dependencies for your application. Typically, you'll be using the Google Maven repository to retrieve some Android dependencies.

```sakerscript
$dependencies = saker.maven.resolve(
	Artifacts: [
		"androidx.multidex:multidex:2.0.1",
		"androidx.drawerlayout:drawerlayout:1.0.0"
	],
	Configuration: {
		Repositories: [
			{
				Id: google,
				Url: "https://maven.google.com/",
			}
		]
	}
)

$libs = saker.android.aapt2.compile(
	$dependencies,
	Identifier: libs
)
```

The above will simply resolve the dependencies of `androidx.multidex:multidex:2.0.1` and `androidx.drawerlayout:drawerlayout:1.0.0` artifacts and compile them as an Android library. You can pass the output of the Maven dependency resolution as the input for the aapt2 linker task.

If you're using saker.build in an IDE, then the sources of the artifacts will also be downloaded so you can use them inside the IDE.

## Dexing

The [`saker.android.d8()`](/taskdoc/saker.android.d8.html) task will transitively include all classpath in the dexing operation, so you don't need to deal with adding each dependency individually as the input.

```sakerscript
$javac = saker.java.compile(### ... ###)
saker.android.d8($javac)
```
