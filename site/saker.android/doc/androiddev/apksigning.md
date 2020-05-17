# APK signing

Each APK must be digitally signed before installing them on Android devices. This can be done using the [apksigner](https://developer.android.com/studio/command-line/apksigner) tool. The [`saker.apk.sign()`](/taskdoc/saker.apk.sign.html) build task can be used to perform the signing.

Also note that if you're creating a release APK you should perform [zip alignment](zipaligning.md) **before** signing the APK.

By default, simply calling the [`saker.apk.sign()`](/taskdoc/saker.apk.sign.html) task will sign the APK with an automatically generated debug key:

```sakerscript
$apk = saker.apk.create(### ... ###)
$signed = saker.apk.sign($apk)
```

An APK that is signed using the debug key can be used to install and test on your own devices. You are not recommended to distribute these APKs to others as you may not be able to properly update them later.

## Using a keystore

For signing the APK with a specific key, use the `Signer` parameter of the [`saker.apk.sign()`](/taskdoc/saker.apk.sign.html) task:

```sakerscript
saker.apk.sign(
	$apk,
	Signer: {
		KeyStore: keystore,
		Alias: keyalias,
		StorePassword: storepass,
		KeyPassword: keypass,
	}
)
```

The `KeyStore` parameter specifies the path to the keystore that contains the signer key. Substitute the appropriate fields with the values for your own keystore.

Generating a key can be done using [`keytool`](https://docs.oracle.com/javase/8/docs/technotes/tools/unix/keytool.html). One simple example for generating a keystore is the following command line:

```plaintext code-wrap
keytool -genkey -noprompt -keystore keystore -alias keyalias -storepass storepass -keypass keypass -keyalg RSA -validity 35600 -dname "CN=Android Test, OU=, O=Android, L=, S=, C=US"
```
