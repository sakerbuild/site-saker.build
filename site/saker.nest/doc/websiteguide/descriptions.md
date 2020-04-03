# Descriptions

Each package that is published on the saker.nest repository will have a general overview page that the details for it are displayed for others. This page contains informations about the package that you can fill in your package management page.

Such displayed informations are short descriptions about the purpose of the package, descriptions about each bundle and task in package, and changelog for the released versions.

The information site can also display links to the source code repository where the code associated with a given package is stored, licensing information, and link to the documentation.

Tags can also be assigned to the packages for easier searchability. For example tags such as `#Java` or `#C++` can be added to packages that are to be used with the given languages. Tags are limited to a maximum of 10, and we recommend not overusing them if not necessary.

## Markdown

When writing descriptions for the packages, bundles, and tasks, markdown syntax can be used. This is in order to provide basic formatting of the conveyed information. However, we decided to limit the usage of images and links in the rendered markdown, as they can be prone to abuse. Displayed images can easily disrupt the UI of the site, while links can be abused to link unrelated sites or resources. (HTML tags are also forbidden.)

Any images are straight removed, while links are replaced with the contents of theirs, resulting in the plain representation of the link text.

We also style various rendered elements to have a smaller representation (like headers), in order to prevent abuse. We recommend keeping descriptions short, and to be used in the way they're supposed to. To provide documentation and detailed explanations, use the documentation link field for display.

We use the [markdown-it](https://github.com/markdown-it/markdown-it) library to render the markdown on client side.