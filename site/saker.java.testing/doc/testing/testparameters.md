# Test parameters

The [`saker.java.test()`](/taskdoc/saker.java.test.html) task allows you to pass arbitrary parameters to the test runner and the test cases. They can be used to convey information about the environment, or otherwise configure the testing in and use-case dependent way.

Note that in order to pass parameters to the tests, the test runner, and the test cases should support them as well.

Use the [`TestInvokerParameters`](/taskdoc/saker.java.test.html#TestInvokerParameters) and [`TestClassParameters`](/taskdoc/saker.java.test.html#TestClassParameters) build task parameters to specfy the test invoker and test case parameters.
