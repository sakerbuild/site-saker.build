# Preparing appx

Preparing an appx means that you construct the hierarchy of the application contents. The files that will be packaged into your final *.appx* file will be set up in the build directory.

The [`saker.appx.prepare()`](/taskdoc/saker.appx.prepare.html) task lets you specify the contents of your application:

```sakerscript
$executable_path = __TOKEN__#...
saker.appx.prepare(
	AppxManifest: manifests/AppxManifest.xml,
	Contents: [
		$executable_path,
		{
			Directory: assets,
			Wildcard: **
		}
	]
)
```

The above simply sets up the application directory with the specified manifest and contents. The prepared application then can be [registered](running.md#registering) and [launched](running.md#launching) if you intent to run it for testing.

It can also be [packaged](packaging.md) and then [signed](sign.md) for distribution.
