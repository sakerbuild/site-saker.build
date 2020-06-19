# Running

During development you'll often run the application in order to properly test it. After you've [prepared](preparing.md) the application directory, you can then register and launch the developed application.

In order to do this, you need to [enable development mode](https://docs.microsoft.com/en-us/windows/uwp/get-started/enable-your-device-for-development) on your PC. After that, the application needs to be registered before it can be launched.

## Registering

Registering an application causes it to be installed in your system. It means that the application that you've previously prepared will be basically installed on your machine. You'll be able run it as you would other applications, but it is also debuggable if necessary.

The registration is done using the [`saker.appx.register()`](/taskdoc/saker.appx.register.html) build task which takes the output of the [`saker.appx.prepare()`](/taskdoc/saker.appx.prepare.html) task as the input.

```sakerscript
$prepared = saker.appx.prepare(### ... ###)
saker.appx.register($prepared)
```

After running the above, you'll see that your application appears in your Start Menu. (If everything goes well.)

You are recommended to specify the `AllowReinstall` parameter as well so in case of incompatible modifications, your application registration can proceed:

```sakerscript
saker.appx.register(
	$prepared,
	AllowReinstall: true
)
```

The task uses the [Add-AppxPackage](https://docs.microsoft.com/en-us/powershell/module/appx/add-appxpackage) PowerShell cmdlet to register the application.

## Launching

After registering your app, you can launch it from the Start Menu, or in any way you like it. If you want to run it as part of your build process, use the [`saker.appx.launch()`](/taskdoc/saker.appx.launch.html) build task:

```sakerscript
$register = saker.appx.register(
	$prepared,
	AllowReinstall: true
)
saker.appx.launch($register)
```

The task accepts the output of the [`saker.appx.register()`](/taskdoc/saker.appx.register.html) task so it can make sure that the application is already registered before attempting to launch it.

You can also launch other applications by specifying their [Application User Model ID](https://docs.microsoft.com/en-us/windows/configuration/find-the-application-user-model-id-of-an-installed-app):

```sakerscript
# Launches the windows calculator
saker.appx.launch(Microsoft.WindowsCalculator_8wekyb3d8bbwe!App)
```

If you don't specify the ID part of the AUMID (*!App* in the case above), the launch task will attempt to figure it on its own.

The task uses the [appxlauncher.exe](https://docs.microsoft.com/en-us/windows/uwp/xbox-apps/automate-launching-uwp-apps#command-line-1) tool to launch the applications.