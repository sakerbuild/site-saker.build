# Immutable collections

The [`ImmutableUtils`](/javadoc/saker/util/ImmutableUtils.html) class provides functionality for working with and creating immutable collections and maps. From our perspective, one of the main advantage of using immutable collections (apart from being immutable of course) is that they can be significantly more memory efficient.

Using an immutable [`NavigableSet`](https://docs.oracle.com/javase/8/docs/api/java/util/NavigableSet.html) instead of a [`TreeSet`](https://docs.oracle.com/javase/8/docs/api/java/util/TreeSet.html) or [`ConcurrentSkipListSet`](https://docs.oracle.com/javase/8/docs/api/java/util/concurrent/ConcurrentSkipListSet.html) can result in significantly fewer objects in memory, and overall less memory usage. Given that those implementations are backed by [`Map`](https://docs.oracle.com/javase/8/docs/api/java/util/Map.html) instances, they will need to have a [`Map.Entry`](https://docs.oracle.com/javase/8/docs/api/java/util/Map.Entry.html) object allocated for each contained element. However, an immutable navigable set implementation can use a simple backing array, that uses less memory, while provides the same O(log n) algorithmic complexity. Being backed by an array can also provide better memory performance as the element references are closer in memory.

We've experienced that when dealing with lot of collections and lot of elements, using the immutable collections can noticeably the memory usage.

#### Externalization

In our use-cases, we needed the collections to be [`Externalizable`](https://docs.oracle.com/javase/8/docs/api/java/io/Externalizable.html) in order to implement a more error-tolerant serialization. The immutable collections that the class provides all implement the [`Externalizable`](https://docs.oracle.com/javase/8/docs/api/java/io/Externalizable.html) interface, unless it is explicitly noted otherwise.

#### Singletions, empties

The class also provides functions for singleton and empty collections additionally to the ones provided by [`Collections`](https://docs.oracle.com/javase/8/docs/api/java/util/Collections.html). Among others, it allows creating sorted singleton maps, and empty sorted sets/maps that return a given comparator.

When you're creating lot of singleton lists, it may be important that our [singleton list](/javadoc/saker/util/ImmutableUtils.html#singletonList-E-) implementation doesn't extend [`AbstractList`](https://docs.oracle.com/javase/8/docs/api/java/util/AbstractList.html) therefore not including the unnecessary 4 byte field [`modCount`](https://docs.oracle.com/javase/8/docs/api/java/util/AbstractList.html#modCount) (neither does our other immutable lists). (In case of large number of singleton lists, the 4 bytes may add up.)
