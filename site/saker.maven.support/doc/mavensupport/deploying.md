# Artifact deploying

<div class="since-version">

Deploying is available since [saker.maven.support-**v0.8.1**](https://nest.saker.build/package/saker.maven.support?version=v0.8.1)

</div>

Artifact deploying is the act of uploading the artifacts to a remote repository. It may also be referenced as *publishing* in some cases.

The artifact deploying usually requires [authentication](authentication.md) from the client.

The deploying can be performed using the [`saker.maven.deploy()`](/taskdoc/saker.maven.deploy.html) task. The task deploys multiple artifacts under the specified coordinates in a batch. Each artifact can be given using a specifier and an artifact path.

The specifiers tell the task the classifier and extension parts of a given artifact. The following example demonstrates it:

```sakerscript
saker.maven.deploy(
	Artifacts: {
		"pom": pom.xml,
		"jar": artifact.jar,
		"sources:jar": artifact-sources.jar,
	},
	Coordinates: "example.groupid:example.artifactid:0.1",
	RemoteRepository: {
		Id: github,
		Url: "https://maven.pkg.github.com/exampleuser/example_repository",
		Authentication: saker.maven.auth.account(
			UserName: exampleuser,
			Password: 123456789abcdef123456789abcdef123456789a,
		)
	},
)
```

The `Artifacts` are specified in a map where the keys are the classifier and extension specifiers, while the values are the paths to the deployed artifacts.

The actual artifact coordinates under which the artifacts are deployed are constructed by merging the `Coordinates` parameter and the specifier. The above will result in the following artifacts being deployed:

```plaintext
example.groupid:example.artifactid:pom:0.1            (pom.xml)
example.groupid:example.artifactid:jar:0.1            (artifact.jar)
example.groupid:example.artifactid:sources:jar:0.1    (artifact-sources.jar)
```
