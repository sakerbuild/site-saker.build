# Format conversion

Serialized property lists can have different formats. The one that you usually encounter is XML based, while there's also a binary format that is more suitable for machine interpretation.

The [`saker.plist.convert()`](/taskdoc/saker.plist.convert.html) task can be used to convert between these formats as you like.

```sakerscript
saker.plist.convert(
	Info.plist,
	Format: binary1
)
```

The above will simply convert the input *Info.plist* to binary format and write the result to the build directory. (Note that the `binary1` format identifier comes from the format name parameter for the *plutil* tool)

Other plist manipulation build tasks that have a `Format` parameter can also be used to modify and convert a property list in a single step. \
E.g. when using the [`saker.plist.insert()`](/taskdoc/saker.plist.insert.html) task:

```sakerscript
saker.plist.insert(
	Input: Info.plist,
	Values: # ...
	Format: binary1
)
```
