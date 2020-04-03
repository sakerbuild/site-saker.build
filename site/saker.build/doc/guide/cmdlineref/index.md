[!section](build.md)
[!section](daemon.md)
[!section](action.md)
[!section](licenses.md)
[!section](version.md)

# Command line reference

<div class="doc-cmdref-cmd-usage">

[@command-file] [subcommand] ...

</div>

<div class="doc-cmdref-cmd-doc">

saker.build system command line interface.

See the subcommands for more information.

Any parameters that take &lt;address&gt; as their arguments 
are expected in any of the following format:

    hostname
    hostname:port
    ipv4
    ipv4:port
    [ipv6]
    [ipv6]:port

Where the host names will be automatically resolved, and 
the IPv6 addresses need to be surrounded by square brackets. 
If the port numbers are omitted, the default port associated 
for the given setting will be used.

</div>

##### Sub-commands

[!tableofcontents]()

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

