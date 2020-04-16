# main

<div class="doc-cmdref-cmd-usage">

main [@command-file] [parameters] arguments...?

</div>

<div class="doc-cmdref-cmd-doc">

Invokes the main method of a specified bundle.

The command will load the bundle with the given name
and invoke the main method of it. The command can be
configured similarly as you can configure the repository
for build executions.

Any extra arguments specified will be passed to the main
method of the bundle.

The command will search for the methods intMain(String[]) or
main(String[]). It will invoke the one it first finds. If the 
invoked method is declared to return an integer, then that will 
be used as an exit code of the process. Use the -system-exit flag 
to control how this value is interpreted.

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


##### -class

<div class="doc-cmdref-param-aliases">-class &lt;string&gt;
</div>

<div class="doc-cmdref-param-flags">
</div>

<div class="doc-cmdref-param-doc">

Specifies the name of the class to invoke.

This parameter is used to specify the name of the Java
class that is loaded and used to invoke its main method.

If this argument is not specified, the Main-Class attribute
of the bundle manifest will be used.

</div>


##### -bundle

<div class="doc-cmdref-param-aliases">-bundle
</div>

<div class="doc-cmdref-param-flags">
</div>

<div class="doc-cmdref-param-doc">

The identifier of the bundle which should be invoked.

The bundle with the specified identifier will be loaded
(possibly downloaded from the internet) and the main class
of it will be invoked.

If the specified bundle identifier has no version qualifier,
then it will be resolved to the most recent version of it.

If this parameter is not specified, the first value for the
arguments parameter is used as bundle identifier.

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


##### -system-exit

<div class="doc-cmdref-param-aliases">-system-exit &lt;enum&gt;
</div>

<div class="doc-cmdref-param-flags">
</div>

<div class="doc-cmdref-param-doc">

Specifies how and when the current process should be exited.

This parameter controls whether or not the current process 
should be exited at the end of the main method invocation.

Exit codes are determined the following way:
 -  0: if the main method invocation is successful
 - -1: if an exception was thrown
 - int: the value returned by the main method (if any)
 - none: if there is no exit code

The values may be the following:
 - always
   The current process will always be exited.
   0 exit code will be used if there was none.
 - on_exception
   The process will only exit, if there was an exception
   thrown by the main method.
   The exit code will be -1.
 - forward
   The process will exit, if there was an exception, or
   the main method returns an integer.
 - never
   The command will never cause the current process to exit.
   (Note that the main invocation may still do so.)

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


##### arguments...


<div class="doc-cmdref-param-flags">
Positional parameter. 
</div>

<div class="doc-cmdref-param-doc">

The arguments that are passed as the input to the main method
of the invoked class.

Zero, one, or more strings that are directly passed as the
String[] argument of the main method.

If no -bundle is specified, the bundle identifier is determined
using the first value of this parameter. In that case that value
will not be part of the input arguments.

</div>

