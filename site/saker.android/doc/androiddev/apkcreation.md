# APK creation

An APK (Application Package File) is the final result archive that contains the code and resources of your application. It needs to be properly packaged so you can install it on your devices.

The [`saker.apk.create()`](/taskdoc/saker.apk.create.html) task lets you create the APK by including the appropriate assets, resources, code, and manifest in it.

```sakerscript
$aapt2link = saker.android.aapt2.link(### ... ###)
$d8 = saker.android.d8(### ... ###)
saker.apk.create(
	Resources: $aapt2link
	Classes: $d8
	Assets: assets/
)
```

The above simply creates the APK that contains the resources that is the result of the previous aapt2 link operation, and the classes that were converted to dex format using d8. All asset files will be included from the `assets/` directory of the project.

If you're creating multiple APKs, you can specify the `Output` parameter that generates the APK with a different name:

```sakerscript
foreach $assetdir in [assets-en, assets-de] {
	saker.apk.create(
		Assets: [
			assets
			$assetdir
		]
		Output: "apk-{ $assetdir }.apk"
		# ...
	)
}
```

The above generates two APKs that include all files from the main `assets/` directory, and in addition to that they include the assets from the specified `assets-en` or `assets-de` directories.

After you've created the APK, you'll need to digitally sign it before you can install it on Android devices. See [](apksigning.md) for more information.