# Android resources

[Resources](https://developer.android.com/guide/topics/resources/providing-resources) in your Android app are additional files alongside your code that you can reference and use in your application. Most commonly they are images, sound, layout files, or other static content that you use with your app.

Android resources need to be processed before including them in your application bundle. The [AAPT2](https://developer.android.com/studio/command-line/aapt2) tool is used to compile and link them together. This is necessary so your application can properly reference and use them.

The processing of resources happens in two phases: compiling and linking. Compiling is performed individually for each resource file while linking merges them into a resource APK that will be part of your final app bundle. The compiling can be performed with the [`saker.android.aapt2.compile()`](/taskdoc/saker.android.aapt2.compile.html) task.

The linking is performed by the [`saker.android.aapt2.link()`](/taskdoc/saker.android.aapt2.link.html) task. It requires the compiled resources as the input, and in addition to that, the path th your `AndroidManifest.xml`. The manifest file describes the main structure and core information about your app.

If you have a simple project hierarchy as:

```plaintext
src/
    MyActivity.java
res/
    drawable/
        graphic.png
    layout/
        main.xml
        info.xml
    mipmap/
        icon.png
    values/
        strings.xml
AndroidManifest.xml
```

Then you compiling and linking your resources is as simple as follows:

```sakerscript
$aapt2c = saker.android.aapt2.compile(res)
$aapt2link = saker.android.aapt2.link(
	$aapt2c,
	Manifest: AndroidManifest.xml
)
```

The link task has a ton of various parameters, which you'll likely rarely use. For more information about them refer to the [`saker.android.aapt2.link()`](/taskdoc/saker.android.aapt2.link.html) task documentation page.

The linking process generates the famous `R.java` source file that you can include in your [compilation](java.md) to reference the resources by their ID.

The result of the linking needs to be included in the final APK for your app. See [](apkcreation.md) for more information.

### SDK versions

One common use-case is setting the minimum and target SDK versions for your app. You can do this by setting the [`MinSDKVersion`](/taskdoc/saker.android.aapt2.link.html#MinSDKVersion) and [`TargetSDKVersion`](/taskdoc/saker.android.aapt2.link.html#TargetSDKVersion) parameters of the [`saker.android.aapt2.link()`](/taskdoc/saker.android.aapt2.link.html) task.

```sakerscript
saker.android.aapt2.link(
	MinSDKVersion: 19
	TargetSDKVersion: 29
	# ...
)
```

Using these will set the appropriate attributes in the resulting manifest file.
