# SDK support

A thing to note is that not all build tasks support SDKs automatically. In order to work with SDKs, the task that you want to use SDKs with must support them.

In order to do that, they need to add the saker.sdk.support package as their dependency, and implement the required APIs to be able to take SDKs, and their references as input.

If you find a build task that doesn't support SDKs, but have an use-case for them, make sure to let the authors know.
