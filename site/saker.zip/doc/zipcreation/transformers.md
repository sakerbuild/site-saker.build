# Resource transformers

ZIP resources transformers can alter the contents of the created ZIP archive. They take part in the creation of the archive, and are presented with an opportunity to analyze and modify each persisted ZIP entry. 

Transformers perform their work in a streaming manner, meaning that as the output archive is being written, they can modify the currently written entry.

The transformers are allowed to modify the output archive path, contents, and modification time of a given entry. They are also allowed to create new archive entries, rename or remove them.

The ZIP resource transformers are available via the programmatic API of the saker.zip package. See the [package Javadoc](/javadoc/index.html) for more information.

Users of ZIP resource transformers should examine the documentation for their transformer of interest, and add them to the [`saker.zip.create()`](/taskdoc/saker.zip.create.html) task as it is recommended by the transformer author. The `Transformers` parameter is the entry point for transformer addition.
