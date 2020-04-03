# Configuration

The repository configuration of the tasks can be specified by specifying the `Configuration` parameter:

```sakerscript
saker.maven.resolve(
	# ... other parameters ...
	Configuration: {
		LocalRepositoryPath: "c:/Users/user/.m2/repository",
		Repositories: [
			{
				Id: central,
				Url: "https://repo.maven.apache.org/maven2/",
			},
			{
				Id: jcenter,
				Url: "https://jcenter.bintray.com/",
			}
		]
	}
)
```

In the above example we set the local repository path and the repositories to be used when the dependencies are resolved. The `LocalRepositoryPath` parameter specifies the path to the local repository where the artifacts can be stored and cached. The `Repositories` list the repositories that should be used when resolving artifacts and dependencies.

If no configuration is specified, the local repository path will be `{user.dir}/.m2/repository`. If no repositories are specified, the `central` repository is used with the `Url` of `https://repo.maven.apache.org/maven2/`.

The snapshot and release policy can also be configured for the repositories:

```sakerscript
saker.maven.resolve(
	# ... other parameters ...
	Configuration: {
		LocalRepositoryPath: "c:/Users/user/.m2/repository",
		Repositories: [
			{
				Id: central,
				Url: "https://repo.maven.apache.org/maven2/",
				Snapshots: false,
				Releases: {
					UpdatePolicy: daily
				}
			},
		]
	}
)
```

The above showcases that snapshot artifacts are disabled, and the release artifacts should be updated on a daily basis.
