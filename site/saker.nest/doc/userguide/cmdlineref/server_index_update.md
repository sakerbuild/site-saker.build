# update

<div class="doc-cmdref-cmd-usage">

server index update [@command-file] [parameters]

</div>

<div class="doc-cmdref-cmd-doc">

Updates the index files for the server bundle storage.

The command will freshen up the index files and retrieve
the latest ones from the associated servers.

Invoking this command can be useful if you've just published
a package and want to see its results as soon as possible.
Without the manual update of the index files, seeing the published
package may take some time.

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


##### -storage

<div class="doc-cmdref-param-aliases">-storage &lt;string&gt;
</div>

<div class="doc-cmdref-param-flags">
Multi-parameter. 
</div>

<div class="doc-cmdref-param-doc">

Specifies the names of the configured server bundle storages
of which the index files should be updated.

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

