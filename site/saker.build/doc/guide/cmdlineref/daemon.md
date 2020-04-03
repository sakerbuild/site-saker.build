[!section](daemon_start.md)
[!section](daemon_stop.md)
[!section](daemon_run.md)
[!section](daemon_io.md)
[!section](daemon_info.md)

# daemon

<div class="doc-cmdref-cmd-usage">

daemon [@command-file] [subcommand] ...

</div>

<div class="doc-cmdref-cmd-doc">

Base command for configuring and managing daemons.

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

