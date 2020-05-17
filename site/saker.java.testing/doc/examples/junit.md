# Using JUnit

In this example we'll show the test configuration that can be used to run Java test with JUnit. In this example we'll do the following:

* Compile our app sources.
* Compile our test sources.
* Locate the JUnit framework and add it to the classpath using Maven.
* Invoke the tests.

```sakerscript
$junitcp = saker.maven.classpath(saker.maven.resolve("junit:junit:4.12"))
$src = saker.java.compile(src)
$tests = saker.java.compile(
	SourceDirectories: test,
	ClassPath: [
		$src,
		$junitcp,
	],
)

saker.java.test(
	TestRunnerClassPath: $junitcp,
	ClassPath: $src,
	TestClassPath: $tests[ClassDirectory],
	TestInvokerParameters: {
		TestRunnerClass: org.junit.runner.JUnitCore
	},
	TestClasses: **.*Test,
)
```

The above is what we consider the absolute minimal example that uses JUnit. Let's break it apart:

The `$junitcp` variable is set to represent a [Maven classpath](root:/saker.maven.classpath/doc/index.html). It is passed as the input for the test compilation, and as well to the test runtime classpath.

The `$src` variable contains the compilation results of our Java app. The sources are under the `src` directory.

The `$tests` variable is the result of the test compilation. It has the `$src` as one of its classpath input, as the tests are supposed to test the classes of our app.

We call the [`saker.java.test()`](/taskdoc/saker.java.test.html) task to invoke the testing. The [`TestRunnerClassPath`](/taskdoc/saker.java.test.html#TestRunnerClassPath) contains the JUnit testing framework. The [`ClassPath`](/taskdoc/saker.java.test.html#ClassPath) contains the classes that are the subject of testing.

The [`TestClassPath`](/taskdoc/saker.java.test.html#TestClassPath) parameter is set to the **class directory** of the test compilation result. We don't want to pass the direct result of the `$tests` compilation, as that would transitively include the input classpaths of the compilation as well. However, as the necessary runtime dependencies are already added in the [`TestRunnerClassPath`](/taskdoc/saker.java.test.html#TestRunnerClassPath) and [`ClassPath`](/taskdoc/saker.java.test.html#ClassPath), therefore we need to prevent adding them multiple times to the test classpath.

We set the [`TestRunnerClass`](/javadoc/saker/java/testing/api/test/invoker/ReflectionJavaTestInvoker.html#PARAMETER_TEST_RUNNER_CLASS) test invoker parameter to tell the test invoker to use the [`org.junit.runner.JUnitCore`](https://junit.org/junit4/javadoc/latest/org/junit/runner/JUnitCore.html) entry point for the testing framework.

The [`TestClasses`](/taskdoc/saker.java.test.html#TestClasses) parameter set the test case classes based on their names. We run the tests for every class in the [`TestClassPath`](/taskdoc/saker.java.test.html#TestClassPath) that end with `Test`. 