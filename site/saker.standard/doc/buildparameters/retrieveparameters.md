# Retrieve parameter values

The tasks in the package allows accessing the [execution](root:/saker.build/doc/guide/execparams.html) and [environment](root:/saker.build/doc/guide/envconfig.html) user parameters during the build execution.

The [`std.param.env()`](/taskdoc/std.param.env.html) and [`std.param.exec()`](/taskdoc/std.param.exec.html) tasks can be used in the build scripts to retrieve the given user parameters respectively:

```sakerscript
$envparam = std.param.env(example.env.parameter)
$execparam = std.param.exec(example.exec.parameter)
```

If we configure the build execution with the following command line:

```plaintext
-EUexample.env.parameter=123 -Uexample.exec.parameter=abc
```

The the value of `$envparam` will be `123`, while the value of `$execparam` will be `abc`.

If the parameter for the specified name wasn't found, then the tasks will throw an appropriate exception signaling the error. You can optionally use the `DefaultValue` that should be returned if the parameter is not found, or specified as `null`.
