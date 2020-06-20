# Modifying plists

Modifying property lists is possible using the [`saker.plist.insert()`](/taskdoc/saker.plist.insert.html) build task. It will insert the specified entries into the property list and write the output in the build directory.

```sakerscript
saker.plist.insert(
	Input: Info.plist,
	Values: {
		CFBundleVersion: "1.2.3"
	}
)
```

The above simply adds the `CFBundleVersion: "1.2.3"` pair to the property list at the path `Info.plist` and writes the output in the build directory. It is a simple way of dynamically setting the build version of your application.

You can also create new property lists by not specifying an input file:

```sakerscript
saker.plist.insert(
	Values: {
		MyKey: MyString,
		MyNumber: 123,
		MyArray: [
			456
		],
		MyBoolean: true,
		MyDictionary: {
			InnerKey: InnerValue
		}
	}
)
```

The result will contain only the specified values. You can see that the specified dictionaries and array will be added into the property list with their appropriate types.

## SDK values

You can also insert values into property lists based on SDKs.

```sakerscript
saker.plist.insert(
	Input: Info.plist,
	Values: {
		BuildMachineOSBuild: sdk.property(DevMacOS, Identifier: build.version),
	},
	SDKs: {
		DevMacOS: saker.apple.sdk.dev_macos()
	}
)
```

In the case above, the `BuildMachineOSBuild` property will be set to the build version of the macOS that is used to develop the application on.

## Removing a value

You can remove a value from the property list by setting the corresponding value to `null`:

```sakerscript
saker.plist.insert(
	Input: Info.plist,
	Values: {
		UIStatusBarStyle: null
	}
)
```

The above simply removes the `UIStatusBarStyle` entry from the property list.

## Output format

By default, the task keeps the output format the same as the input property list. However, you can use the `Format` parameter to specify how the output should be serialized:

```sakerscript
saker.plist.insert(
	Input: Info.plist,
	Values: # ...
	Format: binary1
)
```

The above will perform the insertion of the values and write the output in `binary1` format. If you leave out the `Values` parameter, only format conversion will be performed, however, we recommend using [`saker.plist.convert()`](/taskdoc/saker.plist.convert.html) if your only intention is to convert the format.

## Inserting preset values

You may need to add specific keys into the *Info.plist* file of your application when distributing them through the App Store. See [](../appdev/preset.md#info_plist-value-insertion) for inserting common values into the *Info.plist* file.
