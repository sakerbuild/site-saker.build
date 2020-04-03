# stop

<div class="doc-cmdref-cmd-usage">

daemon stop [@command-file] [parameters]

</div>

<div class="doc-cmdref-cmd-doc">

Stops the daemon at the specified address.

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


##### -address

<div class="doc-cmdref-param-aliases">-address &lt;address&gt;
</div>

<div class="doc-cmdref-param-flags">
</div>

<div class="doc-cmdref-param-doc">

The address of the daemon to connect to.
If the daemon is not running at the given address, or doesn't accept
client connections then an exception will be thrown.

</div>

