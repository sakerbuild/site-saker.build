# Garbage collection

When keeping an RMI connection open for a long time, it may be necessary to collect the no longer referenced proxies and object. When a proxy is generated for a given object in the RMI connection, it will be strongly referenced by it. This is necessary because if a proxy object is alive on the other endpoint, the backing implementation object cannot be garbage collected.

The Saker.rmi library supports automatic garbage collection of the no longer referenced objects and proxies. This is automatic, and no configuration for this is available.

Once a proxy is no longer referenced, the RMI connection will notify the other endpoint that the associated object can be garbage collected. You may expect longer latencies for garbage collection, as there must occur at least two garbage collection rounds on both endpoints of a connection to completely collect a given object.
