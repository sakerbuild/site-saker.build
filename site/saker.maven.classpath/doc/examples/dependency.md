# Dependency classpath

The [`saker.maven.classpath()`](/taskdoc/saker.maven.classpath.html) task can take the result of the [`saker.maven.resolve()`](root:/saker.maven.support/taskdoc/saker.maven.resolve.html) task as its input:

```sakerscript
saker.java.compile(
	SourceDirectories: src,
	ClassPath: saker.maven.classpath(saker.maven.resolve([
		"junit:junit:4.12",
		"org.slf4j:slf4j-api:1.7.19"
	]))
)
```

In the above, we use the [`saker.maven.resolve()`](root:/saker.maven.support/taskdoc/saker.maven.resolve.html) task to resolve the dependencies of the specified artifacts. This results in them being included in the classpath and as well as the `org.hamcrest:hamcrest-core:1.3` artifact, as `junit` depends on it.

## Classpath scopes

You can limit the scopes of the resolution results in order to create a classpath that contains only dependencies for the given scope(s). The scope selection mechanism described in the [](root:/saker.maven.support/doc/examples/scopeselection.html) example can be applied:

```sakerscript
$resolved = saker.maven.resolve(Dependencies: {
	"junit:junit:4.12": test,
	"org.slf4j:slf4j-api:1.7.19": compile,
	"javax.servlet:servlet-api:2.5": provided
})
saker.java.compile(
	SourceDirectories: src,
	ClassPath: $resolved[Scopes][Compilation]
)
```

Using the `[Scopes][Compilation]` property of the resolved dependencies will result in the classpath containing dependencies for the `compile` and `provided` scopes.

The above compilation will have the following artifacts on the classpath:

```plaintext
org.slf4j:slf4j-api:jar:1.7.19 - compile
javax.servlet:servlet-api:jar:2.5 - provided
```

Note that `junit` is not in it, as that has the `test` scope, which is not part of the `Compilation` limiter property.

See [](root:/saker.maven.support/doc/examples/scopeselection.html) for more information about scope limiting.