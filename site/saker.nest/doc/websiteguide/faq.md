# FAQ

The following sections contain Frequently Asked Questions and their answers.

##### What kind of resources can be included in the uploaded bundles?

Any kind of content can be present in the uploaded bundles that make sense from an use-case perspective. Be noted, that the saker.nest repository functions mainly for hosting software development related content. With this in mind, the bundles shouldn't contain generic content and shouldn't be used as a general purpose file storage.

Also make sure to have the appropriate rights to upload the contents.

##### Can I prevent my bundle being downloaded by others?

No. Saker.nest is a public package storage to be used by the community. Currently we don't support hosting private packages.

However, note that a self hosted solution for private content is on the [roadmap](/doc/roadmap.md) for the repository. Also given enough demand, we'll be looking into implementing private namespaces in the saker.nest repository.

Note that you can provide a restrictive license for your uploaded bundles, however, saker.nest doesn't enforce it, and it is your responsibility as the uploader to ensure it's not violated. (See also: [terms of service](https://nest.saker.build/terms))

##### Do I need to publish sources with my bundles?

No, but we **strongly** recommend doing so. Publishing the sources alongside your implementation bundles can make easier for consumers to debug and build upon your bundles.

We recommend having a single bundle with the `-sources` qualifier that includes the sources for other bundles in the release. (Don't forget to include the `Nest-Bundle-Source` attribute in the bundle manifests!)

##### Where can I host documentation for my packages?

Any static content hosting should be sufficient. We recommend publishing both the source code and documentation for your published packages. [GitHub](https://github.com/) with [GitHub Pages](https://pages.github.com/) provide both solutions on a single platform.

##### Is there a limit how many packages and tasks I can allocate?

Yes. These limits apply to each registered user and can be modified by the administrators. We also reserve the right to modify these limits as we see fit.

* Default maximum claimed task count: 50
* Default maximum allocated package count: 20
* Default maximum uploaded bundle count per package per version: 20

##### Is there a size limit for uploaded bundles?

Yes. The default limit for an uploaded bundle is 8 MiB (8 388 608 bytes). This applies to each individually uploaded bundle and can be modified by the administrators. We also reserve the right to modify this limit as we see fit.