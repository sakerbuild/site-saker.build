[!section](structure.md)
[!section](packaging.md)
[!section](workflow.md)

# Script language development

Recommended reading:

* [](/doc/guide/scriptlanguages.md)
* [SakerScript overview](/doc/scripting/index.md)

The saker.build system provides an API for custom scripting language implementations that allows close integration with the build execution and IDE support facilities.

The [built-in](/doc/scripting/index.md) language is implemented using this API and all the API is public for creating a fully functional language for the build system. You may choose to develop your own language, or add support for existing languages based on your preference.

The following articles will discuss the necessities for creating a language plugin, and the way of developing and packaging it. We'll see how we can support both build executions and IDE related features.

<div class="doc-wip">

This section of the documentation is still work in progress. We're improving it as our time allows. Until then, please refer to the [JavaDoc](/javadoc/index.html) of the build system. (`saker.build.scripting` and subpackages.)

</div>

##### Table of contents

[!tableofcontents]()
