# install

<div class="doc-cmdref-cmd-usage">

local install [@command-file] [parameters] bundles?

</div>

<div class="doc-cmdref-cmd-doc">

Installs the specified bundle(s) to the local bundle storage.

The command will take the specified bundles and place them
in the local bundle storage. Other agents in the system will
be able to use these installed bundles.

Any already existing bundle(s) with the same identifier will
be overwritten.

</div>

##### @command-file

<div class="doc-cmdref-param-aliases">@command-file
</div>

<div class="doc-cmdref-param-flags">
</div>

<div class="doc-cmdref-param-doc">

File path prefixed with '@' to directly include arguments from the 
specified file. Each argument is on its separate line. They are
directly inserted in place of the @command-file argument. 
The argument can appear anywhere on the command line. Escaping
is not supported for arguments in the command file. 
The file path may be absolute or relative.

E.g: @path/to/arguments.txt

</div>


##### -repo-id

<div class="doc-cmdref-param-aliases">-repo-id &lt;string&gt;
</div>

<div class="doc-cmdref-param-flags">
</div>

<div class="doc-cmdref-param-doc">

Specifies the identifier of the repository.

The identifier is used to properly determine the 
configuration user parameters from the -U arguments.

It is "nest" by default.

</div>


##### -storage

<div class="doc-cmdref-param-aliases">-storage &lt;string&gt;
</div>

<div class="doc-cmdref-param-flags">
</div>

<div class="doc-cmdref-param-doc">

Sets the name of the local bundle storage to where the bundle(s)
should be installed.

It is "local" by default.

</div>


##### -U

<div class="doc-cmdref-param-aliases">-U&lt;key&gt;=&lt;value&gt;
</div>

<div class="doc-cmdref-param-flags">
Multi-parameter. 
</div>

<div class="doc-cmdref-param-doc">

Specifies the user parameters for configuring the repository.

This string key-value pairs are interpreted the same way as the
-U user parameters for the build execution.

</div>


##### bundles


<div class="doc-cmdref-param-flags">
Positional parameter. 
</div>

<div class="doc-cmdref-param-doc">

One or more paths to the bundles that should be installed.

The paths can also be wildcards, e.g. *.jar to install
all Java archives in the current directory as saker.nest bundles.

</div>

