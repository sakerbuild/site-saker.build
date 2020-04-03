# Thread utilities

The [`ThreadUtils`](/javadoc/saker/util/thread/ThreadUtils.html) class defines utility functions and classes that can be used to create and manage threads. It defines a [`ThreadWorkPool`](/javadoc/saker/util/thread/ThreadUtils.ThreadWorkPool.html) interface that is used to provide access to various kinds of thread pools (fixed, dynamic).

The class allows easily running various short or longer tasks that are managed in various ways. It is aimed to provide optimal thread consumption when executing multiple operations in parallel. The functions are aimed to be most helpful when using them as part of build tasks in the [saker.buildaker.build system](root:/saker.build/index.html).

If you need, we recommend to choose an appropriate thread pool implementation that suits your needs. (Be it the one provided by the Java Runtime, by this library, or others.)
