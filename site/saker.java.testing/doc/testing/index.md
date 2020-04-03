# Overview

The package provides the [`saker.java.test()`](/taskdoc/saker.java.test.html) build task that can be used during build execution to test the specified classes.

The task is designed to be generic, meaning that any kind of tests can be executed (unit, integration, etc...), and different test runners can be used that manage the test invocation. It also provides an API that can be used to define your own test runner.

During the testing, the task will spawn a new `java` process(es) that execute the test cases. The task will apply instrumentation to the tested classes to properly determine the dependencies of each test case.
