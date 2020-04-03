# Artifact installing

<div class="since-version">

Installing is available since [saker.maven.support-**v0.8.1**](https://nest.saker.build/package/saker.maven.support?version=v0.8.1)

</div>

Artifact installing is the process of putting an artifact into the local Maven repository. After the installation, agents in the current system can retrieve that artifact to use it.

Installing artifacts doesn't require any authentication, as that is performed on the local machine that you have authority over.

Installing an artifact can be done by simply providing the coordinates and artifact file:

```sakerscript
saker.maven.install(
	ArtifactPath: path/to/artifact.jar,
	Coordinates: "example.groupid:example.artifactid:0.1",
	Configuration: # ...
)
```

The artifact can have any format, you are not required to put `pom.xml` or other meta-data in it. It may not even be necessary that it is a JAR file. However, it is recommended that you adhere to the conventions of the environment. If you install to a repository that is shared with others, make sure to include any necessary meta-data.

You may also want to install the `pom.xml` that declares the artifact dependencies and other data. In order to do so, you can set the `pom` extension in the artifact coordinates:

```sakerscript
saker.maven.install(
	ArtifactPath: path/to/pom.xml,
	Coordinates: "example.groupid:example.artifactid:pom:0.1"
)
```

If not specified, the extension part of the coordinates will default to `jar`.

## Installing a JAR

While the above example simply installs an a artifact with a specified path, you can create and install JAR artifacts as follows:

```sakerscript
$jar = saker.jar.create(
	# create a JAR file with your contents
)
saker.maven.install(
	ArtifactPath: $jar[Path],
	Coordinates: "example.groupid:example.artifactid:0.1"
)
```
