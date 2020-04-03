# Serialization

The build system uses a build database to store build related data on a persistent storage. This database contains the results of tasks, file related informations, and other meta-data for incremental build support.

Task developers should be aware that the [`TaskFactory`](/javadoc/saker/build/task/TaskFactory.html) instances and the results of task invocations will be serialized after the build finishes. This requires that the related objects are serializable in some way. This means that the objects should at least implement `Serializable`, but **strongly recommended** that they provide their own serialization mechanism by implementing `Externalizable`.

The build system uses its own serialization implementation to have a more robust and customizable way of serializing objects rather than using the built-in `ObjectOutputStream` of the Java language. 

The serializer will write the object to the stream by determining its type, and if it is a simple value object (like `Integer`, enum, primitive array, or others) then write them as is. If if it is an `Externalizable` instance then the `writeExternal` method will be called with the serializer as its `ObjectOutput` argument. If the serializer cannot properly serialize the object, it will try to fall back to the `ObjectOutputStream` implementation, and emit a warning about the failure. 

Similarly to `ObjectOutputStream`, the object references will be kept, and a given object will be only written out once to the stream. The implementation may deduplicate simple value objects (like strings) if there are multiple occurrences of equal objects.

The serialization implementation ignores any Java serialization related fields and methods. This means that the serializer will not handle the `serialVersionUID` field declaration, special methods like `writeReplace` and `readResolve` and any other specializations. The implementations of a class should define their own serialization format by implementing `Externalizable`. (Note that `Externalizable` implementations must have a no-arg public constructor as well.)

If the serialization or deserialization of an object fails, then the serializer will attempt to recover from the error, and keep the stream in a consistent state. Unlike `ObjectOutputStream` and `ObjectInputStream`, this is a significant advantage, as while in case of error those implementations will reset their streams, the build system serializer will allow further writing or reading from the streams without a hard failure.

It is also important that the serialization implementation of objects must be side-effect free, meaning that they should not allocate any managed resources, or shouldn't modify any outside state.

Note that the serializer may be modified and improved in the future. Updating the build system may result in a full rebuild for your project and that is an acceptable behaviour. Updating task implementations that your project uses can also result in reinvocation of the associated tasks.

## SerialUtils

The `saker.util.io.SerialUtils` class can help the developers to efficiently implement serialization mechanism. It provides utility functions for serializing simple and complex objects.

It also provides functions that can have significantly better performance when deserializing collections.

We also recommend to use immutable collections and objects as part of task factories, task results, and other objects.
