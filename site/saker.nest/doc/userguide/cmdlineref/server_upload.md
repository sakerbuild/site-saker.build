# upload

<div class="doc-cmdref-cmd-usage">

server upload [@command-file] [parameters] file

</div>

<div class="doc-cmdref-cmd-doc">

Uploads a single bundle to the specified saker.nest repository.

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


##### -server

<div class="doc-cmdref-param-aliases">-server &lt;string&gt;
</div>

<div class="doc-cmdref-param-flags">
</div>

<div class="doc-cmdref-param-doc">

The URL of the server to which the upload should be performed.

It is https://api.nest.saker.build by default.

</div>


##### -overwrite

<div class="doc-cmdref-param-aliases">-overwrite
</div>

<div class="doc-cmdref-param-flags">
</div>

<div class="doc-cmdref-param-doc">

Flag specifying that the already existing bundles can be overwritten.

If not set, the server will decide whether or not the bundles may be
overwritten.

</div>


##### -api-key

<div class="doc-cmdref-param-aliases">-api-key &lt;base64&gt;
</div>

<div class="doc-cmdref-param-flags">
<b>Required parameter.</b> 
</div>

<div class="doc-cmdref-param-doc">

Specifies the API Key to be used for the upload request.

The argument is expected to be in URL safe Base64 format.

</div>


##### -api-secret

<div class="doc-cmdref-param-aliases">-api-secret &lt;base64&gt;
</div>

<div class="doc-cmdref-param-flags">
<b>Required parameter.</b> 
</div>

<div class="doc-cmdref-param-doc">

Specifies the API Secret to be used for the upload request.

The argument is expected to be in URL safe Base64 format.

</div>


##### file

<div class="doc-cmdref-param-format">&lt;file-path&gt;</div>

<div class="doc-cmdref-param-flags">
<b>Required parameter.</b> 
Positional parameter. 
</div>

<div class="doc-cmdref-param-doc">

Path to the bundle to upload.

The specified Java archive should be a valid saker.nest bundle.
If not, an exception is thrown before the upload request is initiated.

</div>

