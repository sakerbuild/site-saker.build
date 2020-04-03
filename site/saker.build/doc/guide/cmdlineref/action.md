# action

<div class="doc-cmdref-cmd-usage">

action [@command-file] [parameters] arguments...?

</div>

<div class="doc-cmdref-cmd-doc">

Invoke an action of a given repository.

Repository actions are arbitrary commands that a repository defines.
They are basically a main function of the repository that can execute
various operations based on the arguments passed to it.

Repositories are not required to support this, it is optional.
See the documentation for the associated repository for more information.

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


##### -storage-directory

<div class="doc-cmdref-param-aliases">-storage-directory &lt;path&gt;
-storage-dir &lt;path&gt;
-sd &lt;path&gt;
</div>

<div class="doc-cmdref-param-flags">
</div>

<div class="doc-cmdref-param-doc">

Specifies the storage directory that the environment can use
to store its files and various data.

This is recommended to be the same that you use as the 
build environment storage directory.

</div>


##### -repository

<div class="doc-cmdref-param-aliases">-repository &lt;classpath&gt;
-repo &lt;classpath&gt;
</div>

<div class="doc-cmdref-param-flags">
Multi-parameter. 
</div>

<div class="doc-cmdref-param-doc">

Specifies the classpath of the repository.

The classpath may be an HTTP URL by starting it with the 
'http://' or 'https://' phrases. 
It can also be a file path for the local file system. 

It can also be in the format of 'nest:/version/&lt;version-number&gt;'
where the &lt;version-number&gt; is the version of the saker.nest repository 
you want to use. The &lt;version-number&gt; can also be 'latest' in which 
case the most recent known saker.nest nest repository release is used.

This parameter and -direct-repo cannot be used together.

</div>


##### -repository-class

<div class="doc-cmdref-param-aliases">-repository-class &lt;class name&gt;
-repo-class &lt;class name&gt;
</div>

<div class="doc-cmdref-param-flags">
Multi-parameter. 
</div>

<div class="doc-cmdref-param-doc">

Specifies the name of the repository class to load.

The class should be an instance of 
saker.build.runtime.repository.SakerRepositoryFactory.

If not specified, the Java ServiceLoader facility is used 
to load the repository.

</div>


##### -direct-repo

<div class="doc-cmdref-param-aliases">-direct-repo &lt;local-path&gt;
</div>

<div class="doc-cmdref-param-flags">
</div>

<div class="doc-cmdref-param-doc">

Specifies that the repository should be loaded in a direct way.

When a direct repository is loaded, it is assumed that it was 
loaded by someone else to the specified path, and it can be used
for this action.

The specified path should point to the directory where the classpath
load request was issued.

This parameter is generally used when programatically starting new
processes that execute repository actions. Developers should use
the -repository parameter to specify the classpath instead.

This parameter and -repository cannot be used together.

</div>


##### -saker-jar

<div class="doc-cmdref-param-aliases">-saker-jar &lt;path&gt;
</div>

<div class="doc-cmdref-param-flags">
</div>

<div class="doc-cmdref-param-doc">

Specifies the location of the build system runtime.

The build system requires its distribution JAR for proper operation,
as it may be necessary for some tasks to start new processes 
to do their work.

Under normal circumstances the build system can locate the appropriate
JAR location based on the classpath of the current process. If it fails,
an exception will be thrown and you might need to specify this if required.

The path will be resolved against the local file system, relative paths
are resolved against the working directory of the process.

(If you ever encounter a bug in automatic resolution, please file
an issue at https://github.com/sakerbuild/saker.build/issues)

</div>


##### arguments...


<div class="doc-cmdref-param-flags">
Positional parameter. 
</div>

<div class="doc-cmdref-param-doc">

A list of string arguments that should be passed to the action.

The arguments will be directly passed to the repository to execute.
Optional, zero arguments may be used.

</div>

