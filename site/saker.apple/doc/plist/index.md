# Property list overview

Property lists (also known as plist) are data containers that are extensively used by applications in the Apple ecosystem. One of the most important one is the [*Info.plist*](https://developer.apple.com/library/archive/documentation/General/Reference/InfoPlistKeyReference/Articles/AboutInformationPropertyListFiles.html) file that describes essential information about the apps.

The saker.apple package supports dynamically manipulating property lists during the build. This is mostly used when build configuration related data needs to be inserted into the *Info.plist* file, or the plist format needs to be converted between binary and XML.

<small>

Note that the saker.apple package only supports plist manipulation when running on macOS. While this seems obvious, you shouldn't expect to be able to work with property lists on non-macOS operating systems. The package uses the native [`CFPropertyList`](https://developer.apple.com/documentation/corefoundation/cfpropertylist) class to perform its operations.

</small>

<div class="doc-wip">

Some aspects of the property list handling is still work in progress. Values with date or raw data type is not yet supported.

</div>

See [](../appdev/preset.md#info_plist-value-insertion) for inserting common values into the *Info.plist* file.
