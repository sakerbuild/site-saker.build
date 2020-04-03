# Task caching

<div class="doc-wip">

The build cache facility of saker.build is still work in progress. See [](/doc/guide/buildcache.md).

</div> 


The build system provides the functionality of caching the results of tasks. When tasks can be cached, the build system will attempt to retrieve its results from a shared cache database and make them available in the build system without actually invoking the associated task.

See also: [](/doc/guide/buildcache.md).

In order for a task to be cacheable, they need to declare the capability that allows the build system to handle the task in a cacheable way. This capability can be reported by overriding the appropriate function of [`TaskFactory`](/javadoc/saker/build/task/TaskFactory.html). See [](taskcapabilities.md).

Tasks which want to be cacheable are recommended to adhere to the following restrictions:

* The task identifier for the task should have a stable [`hashCode`](https://docs.oracle.com/javase/8/docs/api/java/lang/Object.html#hashCode--). This means that the task identifier should return the same hash code for the same objects between different executions of the Java process. This usually requires that the task identifier doesn't derive its hash code from the identity hash code, class hash code, or in any way runtime dependent values. With that in mind, enums cannot be used as task identifiers, because their hash code is not stable.
* The task cannot wait on the result of another task, but it can only retrieve its finished results. This means that the task may only use the finished result retrieval methods of other tasks. This requirement is aligned with the [computation token](taskcapabilities.md#computation-tokens) usage.

The above restrictions are not hard restrictions, meaning that in case of their violation, the build runtime will not throw an exception, but just ignore the task instance for possible build cache usage.

The above restrictions are required in order to provide an efficient and sane implementation for the build system, and may be lifted in the future, but task implementations should align their behaviour with these in place nonetheless.

As a general rule of thumb, only tasks should report this capability which do more work than the time it takes to retrieve their results from a network cache. That is, the time the task computation takes should outweight the network communication times.
