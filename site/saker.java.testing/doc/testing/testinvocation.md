# Test invocation

See also: [](classpath.md)

The [`saker.java.test()`](/taskdoc/saker.java.test.html) build task needs to set up the testing environment and will invoke the test cases one by one. The environment that needs to set up includes the following:

* Starting a new JVM for the tests.
* Apply instrumentation for the spawned JVM.
	* The instrumentation will slightly modify the classes that are being tested in order to record the dependencies that they access.
	* The applied instrumentation can record which methods, fields, classes, and others are being accessed by a test case. It also records file accesses.
	

After the environment has been set up, the build task will ask the test runner to execute the test cases one by one. This process consists of the following:

1. Replace [`System`](https://docs.oracle.com/javase/8/docs/api/java/lang/System.html) properties such as the standard I/O streams and [properties](https://docs.oracle.com/javase/8/docs/api/java/lang/System.html#getProperties--) for the test case.
1. Clear the instrumentation state of the JVM.
1. Pass the test class name and parameters to the test runner.
1. The test runner loads the test case class and executes it.
	* During the execution the class and file dependencies are recorded by the build task.
1. Retrieve the test result from the test runner.
1. Tear down the mocked system properties.

Only a single test can be executed at a time in a single JVM. The JVMs are shared by the test cases, and it is not restarted.

If a test case accesses a file, the file will be taken from the build system hierarchy.