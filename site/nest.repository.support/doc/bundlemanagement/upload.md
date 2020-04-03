# Server upload

See also: [](root:/saker.nest/doc/userguide/serverstorage.html), [](root:/saker.nest/doc/websiteguide/uploading.html)

If you are a saker.nest bundle developer, you may want to share them with the community. The [`nest.server.upload()`](root:/nest.repository.support/taskdoc/nest.server.upload.html) task allows you to perform bundle uploading to the saker.nest API server during the build execution:

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

The task will extract the bundle identifier from the specified bundle, and upload it for the given identifier. The server will validate if the request can be initiated for the given API keys, and allow or deny the upload. The server will validate the uploaded bundle, and you'll be able to view the results on the bundle management page. If the bundle validations succeed, you'll be able to publish the package.

You can specify the `Overwrite` parameter to tell the server whether or not to overwrite any previously uploaded bundle.