# upload

<div class="doc-cmdref-cmd-usage">

server upload [@command-file] [parameters] file

</div>

<div class="doc-cmdref-cmd-doc">


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


</div>


##### -overwrite

<div class="doc-cmdref-param-aliases">-overwrite
</div>

<div class="doc-cmdref-param-flags">
</div>

<div class="doc-cmdref-param-doc">


</div>


##### -api-key

<div class="doc-cmdref-param-aliases">-api-key &lt;base64&gt;
</div>

<div class="doc-cmdref-param-flags">
<b>Required parameter.</b> 
</div>

<div class="doc-cmdref-param-doc">


</div>


##### -api-secret

<div class="doc-cmdref-param-aliases">-api-secret &lt;base64&gt;
</div>

<div class="doc-cmdref-param-flags">
<b>Required parameter.</b> 
</div>

<div class="doc-cmdref-param-doc">


</div>


##### file

<div class="doc-cmdref-param-format">&lt;file-path&gt;</div>

<div class="doc-cmdref-param-flags">
<b>Required parameter.</b> 
Positional parameter. 
</div>

<div class="doc-cmdref-param-doc">


</div>

