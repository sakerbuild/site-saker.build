# Zipaligning

Zipaligning is an optimization of ZIP archives that ensures that the archive entries have proper aligment relative to the start of the file. It is done using the [zipalign](https://developer.android.com/studio/command-line/zipalign) tool.

The alignment should be performed **before** signing your APK as performing the alignment after signing the APK may invalidate the signature.

The optimization is recommended to be used for release builds, while you can omit it for debug builds to speed up build times.

```sakerscript
$apk = saker.apk.create(### ... ###)
$aligned = saker.android.zipalign($apk)
$signed = saker.apk.sign($aligned)
```

The above simply aligns the created APK and then signs it with a debug key.

### Align release builds only

If you want to omit performing alignment for debug builds, the following snippet can be used:

```sakerscript
$release = true
$apk = saker.apk.create(### ... ###)
if $release {
	$signInput = saker.andriod.zipalign($apk)
} else {
	$signInput = $apk
}
$signed = saker.apk.sign($signInput)
```

The above will simply omit zipalignment for non-release builds. You can also write it in a more compact way:

```sakerscript
saker.apk.sign(
	$release 
		? saker.android.zipalign($apk) 
		: $apk
)
```
