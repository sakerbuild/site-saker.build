# Code signing

In order to be able to install your app on other devices, you need to digitally sign it. It can be done by acquiring a certificate from Apple by enrolling in the Developer Program.

You can also sign your applications for development without paying the subscription fee. This is advantageous when creating iPhone apps prior to distribution.

<div class="doc-wip">

The saker.apple package currenty supports signing iPhone applications. Support for signing other types of apps are under work. 

</div>

## Signing iPhone applications

Signing iPhone applications can be done using the [`saker.iphoneos.sign()`](/taskdoc/saker.iphoneos.sign.html) task. It requires the previously created as application bundle as the input, as well as a provisioning profile and signing identity.

The provisioning profile is the one that Apple issued for you to sign your application with. If you let Xcode manage it for you, then it will usually be in the `/Users/<YOUR_USERNAME>/Library/MobileDevice/Provisioning Profiles/` directory.

The signing identity referes to the certificate that is used to sign the application. You can determine an appropriate one by issuing the `security find-identity -v -p codesigning` command:

```plaintext
> security find-identity -v -p codesigning
  1) 0123456789ABCDEF0123456789ABCDEF01234567 "iPhone Developer: myemail@example.com (ABCDEFG123)"
     1 valid identities found 
```

The 40 character hexadecimal identifier should be used as the `SigningIdentity` parameter:

```sakerscript
$bundle = saker.iphoneos.bundle.create(### ... ###)
saker.iphoneos.sign(
	$bundle,
	SigningIdentity: 0123456789ABCDEF0123456789ABCDEF01234567,
	ProvisioningProfile: std.file.local("/Users/User/Library/MobileDevice/Provisioning Profiles/5a7c2ff8-8afe-46a7-9120-a543fe11006f.mobileprovision"),
)
```
