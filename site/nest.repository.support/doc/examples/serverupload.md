# Upload bundle

See also: [](../bundlemanagement/upload.md)

The [`nest.server.upload()`](root:/nest.repository.support/taskdoc/nest.server.upload.html) task allows uploading the created saker.nest bundle during the build execution:

```sakerscript
# Create a bundle JAR
$jar = saker.jar.create(### ... ###)
nest.server.upload(
	Bundle: $jar[Path],
	APIKey: ### API key from bundle page ###,
	APISecret: ### API secret from bundle page ###
)
```

The above example will create a JAR using the [`saker.jar.create()`](root:/saker.jar/taskdoc/saker.jar.create.html) task and upload it to the `https://api.nest.saker.build` API endpoint. You will need the API key and secrets for the package from the saker.nest repository website. You can view the API keys on the Bundles tab of the [bundle management page](https://nest.saker.build/user/packages). The task parameters take them in the same format as they are displayed on the page.
