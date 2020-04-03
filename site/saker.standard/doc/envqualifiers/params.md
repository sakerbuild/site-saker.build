# Parameter qualifier

The [`std.env.qualifier.params()`](/taskdoc/std.env.qualifier.params.html) task can be used to get an environment qualifier that is based on the [environment user parameters](root:/saker.build/doc/guide/envconfig.html) of the build environment.

The created qualifier will expect the specified parameters to have the expected values for them. If they match, the environment is considered to be suitable, otherwise not.

```sakerscript
std.env.qualifier.params(
	Parameters: {
		example.parameter: 123,
		second.param: abc
	}
)
```

The above qualifier will only find an environment suitable if it has the `example.parameter` defined with the value of `123` and `second.param` with `abc`.
