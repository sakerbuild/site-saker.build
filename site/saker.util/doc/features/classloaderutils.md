# ClassLoader utilities

The library contains classes which provide easier implementation of [`ClassLoaders`](https://docs.oracle.com/javase/8/docs/api/java/lang/ClassLoader.html). Our goal was to separate the mechanism of retrieving the data used by the a class loader and the [`ClassLoader`](https://docs.oracle.com/javase/8/docs/api/java/lang/ClassLoader.html) implementation itself. This can result in a pluggable way of creating class loaders where the locating of resources are done by separate implementations.

The [`ClassLoaderDataFinder`](/javadoc/saker/util/classloader/ClassLoaderDataFinder.html) interface is used to define the behaviour of how the resources should be located. It doesn't specify any restrictions on them. Important to note is that this interface extends [`Closeable`](https://docs.oracle.com/javase/8/docs/api/java/io/Closeable.html), so that the owner of the objects need to close the instances. This is important to avoid memory leaks when a class loader is backed by JARs or other managed resources.

The interface is also RMI compatible, meaning that the class data can be provided over the network. This can be especially useful if two Java processes need to communicate and cooperate when execution an operation. (E.g. loading Java classes that need to be tested.)

A [`ClassLoader`](https://docs.oracle.com/javase/8/docs/api/java/lang/ClassLoader.html) can be constructed using the [`MultiDataClassLoader`](/javadoc/saker/util/classloader/MultiDataClassLoader.html) class.
