# Custom files

Creating custom [`SakerFile`](/javadoc/saker/build/file/SakerFile.html) implementations are sometimes beneficial as they can be more memory and performance efficient than using some of the built-in implementations. In order to do that, you need to extend the [`SakerFileBase`](/javadoc/saker/build/file/SakerFileBase.html) class, and implement the appropriate methods.

The main purpose of implementing your own [`SakerFile`](/javadoc/saker/build/file/SakerFile.html) is to provide a more efficient implementation for providing the backing contents of the file. When generating files, it is often more efficient to just keep the generator data in memory rather than the whole contents of the file.

In the following example we're going to showcase a file implementation that simply generates odd numbers up to a maxmimum separated by new lines as the contents of the file.

The class of the file is the following:

[!embedcode](example_customfile/src/example/OddNumbersSakerFile.java "language: java, range-marker-start: class, marker-start-include: true")

The class takes a maximum integer in its content descriptor which is the limit until the file contents list the odd numbers. The [content descriptor class](example_customfile/src/example/OddNumbersContentDescriptor.java) is just a plain `ContentDescriptor` data class containing the maximum integer.

We must override the abstract `writeToStreamImpl` method that is responsible for writing the contents of the file to the argument stream. In the example, we write the odd numbers on separate lines to the stream.

In the overridden method `getEfficientOpeningMethods()` we signal that all content retrieval methods are considered to be efficient. This is related to the implicit synchronization behaviour mentioned in [](filehandling.md#file-contents). As we implement the stream writing efficiently, we can consider all related methods to be efficient as all other implementations use this function in the end.

<small>

Note: An opening method is considered to be efficient if retrieving the contents consume less resources (time and space) than using the hardware disk.

</small>

The above example showcases most of the necessary functionality to implement your own custom file. There are some uncommon extraneous features of the [`SakerFile`](/javadoc/saker/build/file/SakerFile.html) interface which you can get familiar with by viewing the interface documentation.
