# Serialization utilities

The [`SerialUtils`](/javadoc/saker/util/io/SerialUtils.html) utility class provides functions for serializing various kinds of objects. It is mainly useful when a class implements [`Externalizable`](https://docs.oracle.com/javase/8/docs/api/java/io/Externalizable.html) and has to implement its own serialization.

The method serialize object in various formats, which are defined in the documentation of those functions. Each serialization function has a corresponding reading function that can be used to deserialize them.

The class also provides functions for reading collections from the input and creating immutable collections from them. This can be especially important when the serialized object is mutable, but you don't really care about the mutability when it is read back from the stream. Reading back immutable collections can significantly reduce the memory usage similarly to [immutable utils](immutableutils.md).

The reading functions can also read back sorted input from the stream. These functions expect the underlying stream to have the read elements already in sorted order. Reading the elements this way can significantly reduce the deserialization times, as the elements doesn't need to be resorted in order to be deserialized. Reading a sorted set or map from an unsorted stream may take much longer, as the elements need to be inserted one by one instead of just taking their sorted order.

This is similar to creating a [`TreeMap`](https://docs.oracle.com/javase/8/docs/api/java/util/TreeMap.html) using the [`SortedMap`](https://docs.oracle.com/javase/8/docs/api/java/util/SortedMap.html) or the [`Map`](https://docs.oracle.com/javase/8/docs/api/java/util/Map.html) constructor. The sorted creation is much faster, as the elements are already ordered.

The sorted reading of course also has the risk that it doesn't work if the input is not sorted. Note that you need to ensure this yourself, and the sorted reading functions doesn't verify sortedness of the input. Reading non-sorted input using the sorted reading functions may result in unexpected errors down the line.
