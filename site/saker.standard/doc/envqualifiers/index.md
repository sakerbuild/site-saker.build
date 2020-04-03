# Overview

Environment qualifiers are objects that determine the build environmental requirements for an associated task. It is usually passed to a task as an input for it to select a suitable build environment where the given operation can be executed.

Environment qualifiers are often used to select an appropriate environment when a task is being dispatched to build clusters. They are also used by the [`sdk.user()`](root:/saker.sdk.support/taskdoc/sdk.user.html) task to specify the environment that the SDK is defined for.
