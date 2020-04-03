[!section](structure.md)
[!section](packaging.md)

# Repository development

Recommended reading:

* [](/doc/guide/repositories.md)

Task repositories are the primary facility of the build system to provide task and related extensions. Developers can create their own repository implementations in order to provide a custom mechanism of task lookup and related features.

All the API is public for creating a fully functional repository extension, the default [saker.nest repository](root:/saker.nest/index.html) uses only the public API as well. The following articles will provide descriptions and recommendations for implementing a custom repository.  

<div class="doc-wip">

This section of the documentation is still work in progress. We're improving it as our time allows. Until then, please refer to the [JavaDoc](/javadoc/index.html) of the build system. (`saker.build.runtime.repository` and related packages.)

</div>

##### Table of contents

[!tableofcontents]()
