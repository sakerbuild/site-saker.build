# Get execution parameter

See also: [](/doc/buildparameters/retrieveparameters.md)

You can use the [`std.param.exec()`](/taskdoc/std.param.exec.html) task to retrieve the value of an [execution user parameter](root:/saker.build/doc/guide/execparams.html) during build execution.

If you use the following command line to build your project:

```plaintext
-Uexample.exec.parameter=abc
```

Then for the following build script:

```sakerscript
$execparam = std.param.exec(example.exec.parameter)
```

The variable `$execparam` will have the value `"abc"`.
