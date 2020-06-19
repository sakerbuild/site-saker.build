# Signing

In order to distribute your application to your users, you'll need to digitally sign it. This is required so they can be installed on Windows. The Windows Store also requires your applications to be signed.

The signing can be performed using the [`saker.windows.signtool.sign()`](/taskdoc/saker.windows.signtool.sign.html) task. It uses [SignTool](https://docs.microsoft.com/en-us/windows/win32/seccrypto/signtool) to perform the application signing.

```sakerscript
$pkg = saker.appx.package($prepared)
saker.windows.signtool.sign(
	$pkg, 
	Certificate: MyApplicationCertificate.pfx, 
	Algorithm: SHA256
)
```

The above will sign the created *.appx* package and put it in the build directory.

**Note:** When creating App bundles, you should sign the embedded *.appx* packages **before** creating the bundle:

```sakerscript
$pkg = saker.appx.package($prepared)
$pkg_signed = saker.windows.signtool.sign(
	$pkg, 
	Certificate: MyApplicationCertificate.pfx, 
	Algorithm: SHA256
)
$bundle = saker.appx.bundle({
	$pkg_signed[Path]: "",
})
saker.windows.signtool.sign(
	$bundle, 
	Certificate: MyApplicationCertificate.pfx, 
	Algorithm: SHA256
)
```
