# Uploading bundles

After allocating your package name, you'll need to upload the bundles that you want to publish on saker.nest. Bundles can't be uploaded directly through the website, but you need to use the saker.build system to upload a bundle. The bundle uploading can be done in two ways, either during build execution, or via repository actions. In both cases, you'll need the upload API keys for the package.

Bundles can only be uploaded to versions which are not yet published. If the uploaded bundle is the first one to be part of a given package release, a new version entry will be created on the package management page.

## API keys

The API keys are used to authorize the bundle upload network request with the saker.nest API server. The keys are used to sign the uploaded bundle contents and the server will verify that the uploader can upload bundles for the given package.

You can view your API keys in the Bundles page of your created package. The API keys are in [URL encoded Base64](https://tools.ietf.org/html/rfc4648) format and their contents are implementation dependent.

### Regenerating API keys

You can use the *Regenerate API keys* button on the website to regenerate the API keys for bundle uploading. This can be useful if you accidentally misplace your keys or upload them to a public hosting sites like GitHub. Note that even if somebody gets access to your API keys, they won't be able to publish the uploaded bundles without logging in to your account.

After the keys have been regenerated, the previous ones will stop working.

## Build execution

You can use the [`nest.server.upload()`](root:/nest.repository.support/taskdoc/nest.server.upload.html) task to upload your created bundle during build execution. The task takes the path to the uploaded bundle via the `Bundle` parameter, and the API keys using the `APIKey` and `APISecret` paramters.

You can specify whether or not to `Overwrite` already uploaded (but not yet published) bundles, and the `Server` to upload the bundle(s) to. The server defaults to the main API server at: `https://api.nest.saker.build`.

## Repository action

Repository actions can be used to upload the bundle. This doesn't require a build execution to be started. The following command line demonstrates how a given upload can be done:

```plaintext
java -jar saker.build.jar action server upload \
	-api-key <API-KEY> \
	-api-secret <API-SECRET> \
	example.package-v1.0.jar
```

In addition, the `-overwrite` flag can be specified to overwrite already uploaded (but not yet published) bundles, and the `-server` option to specify the upload network endpoint.

## Upload results

As the result of your upload, you will see the uploaded bundle in the Bundles page for your package. The bundle will undergo a first round of validation by the API server, and will display the results for you. If the validation fails, you will need to fix it and reupload the bundle again. The second round of validation will happen during publishing, when additional checks are made to ensure package consistency. The bundle validation may take some time to be processed.

Any uploaded but not yet published bundle may be freely deleted.

If all the bundles pass validation, you'll be able to publish them to the community.
