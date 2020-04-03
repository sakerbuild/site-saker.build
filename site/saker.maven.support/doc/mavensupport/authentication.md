# Authentication

<div class="since-version">

Authentication is available since [saker.maven.support-**v0.8.1**](https://nest.saker.build/package/saker.maven.support?version=v0.8.1)

</div>

When you are working with remote repositories, they may require various kinds of authentication from the client side. You can configure the saker.maven tasks that work with remote repositories to authenticate the following ways.

The authentication configurations can be set on the repository options with the `Authentication` field.

## Account authentication

Account authentication is an username-password pair that you send to the repository to authenticate yourself. The [`saker.maven.auth.account()`](/taskdoc/saker.maven.auth.account.html) task lets you use that.

An example for the above with a GitHub Maven repository:

```sakerscript
saker.maven.download(
	[
		"example.groupid:example.artifactid:0.1"
	],
	Configuration: {
		Repositories: [
			{
				Id: github,
				Url: "https://maven.pkg.github.com/exampleuser/example_repository",
				Authentication: saker.maven.auth.account(
					UserName: exampleuser,
					# Example personal access token
					Password: 123456789abcdef123456789abcdef123456789a,
				)
			}
		]
	}
)
```

The above corresponds to the `<username>` and `<password>` elements of the `settings.xml` schema of Maven.

## Private key

You can use private key to authenticate yourself for the remote repository. This requires a keystore that contains a private key that will be used to sign your requests when made for the repository. The pass phrase is also required to be able to open the given keystore.

This corresponds to the `<privateKey>` and `<passphrase>` elements of the `settings.xml` schema of Maven.

An example for the above:

```sakerscript
saker.maven.download(
	[
		"example.groupid:example.artifactid:0.1"
	],
	Configuration: {
		Repositories: [
			{
				Id: myrepo,
				Url: "https://url/to/the/repository",
				Authentication: saker.maven.auth.privatekey(
					KeyLocalPath: /absolute/local/path/to/private/keystore,
					Passphrase: foo_pass_bar_phrase,
				)
			}
		]
	}
)
```

The exact semantics of how the requests are made to the repository using the keystore and the password are defined by Maven.
