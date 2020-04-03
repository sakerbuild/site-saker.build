# Get environment parameter

See also: [](/doc/buildparameters/retrieveparameters.md)

You can use the [`std.param.env()`](/taskdoc/std.param.env.html) task to retrieve the value of an [environment user parameter](root:/saker.build/doc/guide/envconfig.html) during build execution.

If you use the following command line to build your project:

```plaintext
-EUexample.env.parameter=123
```

Then for the following build script:

```sakerscript
$envparam = std.param.env(example.env.parameter)
```

The variable `$envparam` will have the value `"123"`.
