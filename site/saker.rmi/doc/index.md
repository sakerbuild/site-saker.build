# Overview

Saker.rmi is a Java library that provides RMI (Remote Method Invocation) functionality. It is implemented newly from the ground up, and has no common parts with the [RMI implementation of the Java standard library](https://docs.oracle.com/javase/8/docs/api/java/rmi/package-summary.html).

The goal of Saker.rmi was to develop an RMI library that supports the following features:

* **Cooperation.** The main design point was that the connection endpoints should call each others functions. The method calls are also back-dispatched to the originating threads, therefore `synchronized` blocks don't deadlock in case of issuing new requests when serving one.
* **Transparency.** The code that calls RMI proxy methods don't have to know that its calling an RMI proxy method. The proxies work the same way as if the objects were in the same JVM.
* **Automatic proxy creation.** The RMI proxies used in an RMI connection are automatically created, no additional declarations necessary on the possible proxy interfaces.
* **Ease of transfer configuration.** You can easily customize how a given object is transferred over an RMI connection by either annotating the associated `interface`, or setting them directly on the connection.
* **Fallback and performance.** If an RMI call fails, you can perform fallback operations. You can configure a method to cache its results, meaning that calling them repeatedly won't result in additional network requests.
* **Garbage collection.** When a proxy is no longer referenced by a client, the backing implementation object may be garbage collected. This includes the `Class`es as well.
* **Dynamic class configuration.** Classes which are used by the RMI runtime can be dynamically added and removed.
* **Proxy scopes.** Scopes for RMI communication can be created and closed. These manage the RMI proxies and will release them once the scope is closed. (Scopes are called [`RMIVariables`](/javadoc/saker/rmi/connection/RMIVariables.html).) This allows releasing all used resources after a *"communication session"* is closed.

The library depends on the [saker.util](root:/saker.util/index.html) library for general utilities, and the [ObjectWeb ASM](https://asm.ow2.io/) library for proxy bytecode generation.

The library is included in the [saker.build system](root:/saker.build/index.html) under the `saker.build.thirdparty` package. It is used extensively to implement daemon, build cluster, and other communication layers.
