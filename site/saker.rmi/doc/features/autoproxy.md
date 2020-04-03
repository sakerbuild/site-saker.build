# Automatic proxies

The Saker.rmi library automatically creates proxies when necessary. This is in order to provide the implementational [transparency](transparency.md) for the callers, and for proper cooperation between the connection endpoints.

By default, you can retrieve RMI proxies by either accessing them by name, or explicitly constructing a new object on the other endpoint. However, when calling methods on proxy objects, the arguments or results may be passed as proxy objects rather by serializing them in other ways.

Taking the example from [](backdispatch.md), we work with the following `Stub`:

```java
public interface Stub {
	public int count(IntPredicate predicate);
}
```

When we call the `count()` method with additional RMI connection configuration in place, the argument will be transferred as a new RMI proxy.

```java
Stub s;
s.count(i -> i % 2 == 0);
```

In the above, the lambda argument will be transferred as a proxy. This means that every time its `test()` method is called, a new RMI request is issued on the connection. In this case, the repeated calls of `test()` in a loop may cause performance degradation and we recommend specifying the transfer configuration for that argument in a way that is efficient.

The automatic proxy creation is important to keep the [transparency](transparency.md) requirements of the library, while also allows convenient service declaration. An example for this when one wants to access a specific service from a service provider:

```java
public interface ServiceProviderStub {
	public FileSystemStub getFileSystem();
}
```

In the above no additional configuration is required to call the `getFileSystem()` remote method, the returned object will be automatically transferred as a proxy to the receiver.

Developers should design their interfaces and RMI configurations so the operations can be executed in a performant way.