# Overview

The [saker.sdk.support](https://nest.saker.build/package/saker.sdk.support) package in the [saker.nest repository](root:/saker.nest/index.html) provides features for working with Software Development Kits, their descriptions and references for other tasks in the [saker.build system](root:/saker.build/index.html).

An SDK from a build execution perspective is an immutable installation of resources that can be used by the build in order to perform its tasks. The saker.sdk.support package helps managing and describing the SDKs to be used during the build execution.

It mainly provides a programmatic API for other packages to use, however, the [`sdk.path()`](/taskdoc/sdk.path.html), [`sdk.property()`](/taskdoc/sdk.property.html) and [`sdk.user()`](/taskdoc/sdk.user.html) tasks provide user facing opportunities for working with SDKs.
