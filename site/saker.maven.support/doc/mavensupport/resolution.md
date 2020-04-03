# Dependency resolution

The [`saker.maven.resolve()`](/taskdoc/saker.maven.resolve.html) task supports resolving artifact dependencies. The task can operate with any of the following inputs:

##### Artifact coordinates

You can specify the artifact coordinates for which the dependencies should be resolved:

```sakerscript
saker.maven.resolve(
	Artifacts: [
		"junit:junit:4.12",
		"org.slf4j:slf4j-api:1.7.19"
	]
)
```

In this case the artifact dependencies are resolved using the `compile` scope. In the above scenario the result of the resolution will be:

```plaintext
junit:junit:jar:4.12
org.slf4j:slf4j-api:jar:1.7.19
org.hamcrest:hamcrest-core:jar:1.3
```

The specified artifact coordinates are expected to have the following format:

```plaintext
<groupId>:<artifactId>[:<extension>[:<classifier>]]:<version>
```

##### Dependencies

The dependencies can also be speified with the scopes and exclusions as well:

```sakerscript
saker.maven.resolve(
	Dependencies: {
		"junit:junit:4.12": {
			Scope: test,
			Exclusions: "org.hamcrest:hamcrest-core"
		}
	}
)
```

In the above we specify a dependency on `junit` with the `test` scope, however exclude the transitive inclusion of the `org.hamcrest:hamcrest-core` artifact.

The keys of the `Dependencies` map specify the dependency artifact, while the value map can configure the nature of the dependency. If we don't want to specify exclusions, we can simply pass the scope as the value:

```sakerscript
saker.maven.resolve(
	Dependencies: {
		"junit:junit:4.12": test
	}
)
```

##### `pom.xml`

The task can also resolve the dependencies of a `pom.xml` file:

```sakerscript
saker.maven.resolve(
	Pom: pom.xml
)
```

The task will parse the specified POM file and resolve the dependencies from it. Note that only the dependencies are resolved, other parts of the POM file are note taken into account.


## Output

The output of dependency resolution provide access to the artifact coordinates of the resolved dependencies. You can also query the scope that is associated with the given dependency.

See the [](../examples/scopeselection.md) example for more information.


