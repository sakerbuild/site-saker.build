# Appx overview

[Appx packages](https://docs.microsoft.com/en-us/windows/msix/package/packaging-uwp-apps#types-of-app-packages) are used to distribute Windows applications, more prominently apps for the Universal Application Platform. The saker.windows package provides build tasks for creating, managing, and developing applications that are distributed via the appx format.

Appx is a file format based on the ZIP archive format. This means that the contents of your application are put into an archive which is distributed to your users. The appx package can also be signed, which is required if you intend to upload it to the Windows Store.

The build tasks in saker.windows allows you to create appx packages and also to run them on your local machine for development. 
