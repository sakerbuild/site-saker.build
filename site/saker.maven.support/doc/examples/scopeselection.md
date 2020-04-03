# Scope selection

See also: [Maven dependency scopes](https://maven.apache.org/guides/introduction/introduction-to-dependency-mechanism.html#Dependency_Scope)

When you resolve dependencies, they may have different scopes associated with them. Using the result of the dependency resolution you can select the appropriate set or artifacts that you'd like to use:

```sakerscript
saker.maven.resolve(Dependencies: {
	"junit:junit:4.12": test,
	"org.slf4j:slf4j-api:1.7.19": compile,
	"javax.servlet:servlet-api:2.5": provided
})
```

The result of the dependency resolution will be the following with the scopes included:

```plaintext
junit:junit:jar:4.12 - test
org.hamcrest:hamcrest-core:jar:1.3 - test
org.slf4j:slf4j-api:jar:1.7.19 - compile
javax.servlet:servlet-api:jar:2.5 - provided
```

We may want to select different artifacts based on the operation that we'd like to perform.

For example if we want to select the dependencies for compilation (not for testing):

```sakerscript
$resolved = saker.maven.resolve(### ... ###)
$resolved[Scopes][Compilation]
```

The `[Scopes]` field of the task output allows limiting the dependency set based on the scopes. The `[Compilation]` field will return only the dependencies for the `compile` and `provided` scopes. Applying it to the above resolution result will contain the following artifacts:

```plaintext
org.slf4j:slf4j-api:jar:1.7.19 - compile
javax.servlet:servlet-api:jar:2.5 - provided
```

The result of the scope limitation will have the same semantics as the direct output from the task.

The scope limitation can be also specified for arbitrary scopes:

```sakerscript
$resolved = saker.maven.resolve(### ... ###)
$resolved[Scopes]["test | provided"]
```

In which case the result will contain the `test` and `provided` dependencies:

```plaintext
junit:junit:jar:4.12 - test
org.hamcrest:hamcrest-core:jar:1.3 - test
javax.servlet:servlet-api:jar:2.5 - provided
```

The following pre-defined limitation are available:

* `[Compilation]` equals to `["compile | provided"]`.
* `[Execution]` equals to `["compile | runtime"]`.
* `[TestCompilation]` equals to `["test | compile | provided"]`.
* `[TestExecution]` equals to `["test | compile | runtime"]`.
