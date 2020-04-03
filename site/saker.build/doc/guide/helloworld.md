# Hello world

Let's get started with the simplest build possible. Writing out a message.

```sakerscript
print("Hello world!")
```

The above will call the [`print()`](/doc/scripting/builtintasks/print.md) task that simply prints out the message. The build will successfully finish.

In order to run a build, write the above in a build file called `saker.build`, and run the build by invoking the command: \
(Given that the saker.jar containing the build system runtime is in your working directory. See [](/doc/installation.md).)

```
java -jar saker.jar
```

See the [command line reference](cmdlineref/index.md) for other use-cases of the command line interface.

Running the build again will print out the message again. If you modify the contents of the message, the new message will be printed, and the old will be not.

(Any following command line argument examples will not have the above `java -jar saker.jar` part included.)

## Variables

The build language allows you to declare variables. Variables are a named storage of data that can be assigned once. Each variable lives in the scope of its enclosing build target.

```sakerscript
$sum = 123 + 456
print($sum)
```

Executing the above will print out the value of the `$sum` variable that is `579`.

See also: [](/doc/scripting/langref/variables/index.md).

## Build directory

The build system doesn't use a build directory unless you specify one. This is in order to avoid accidental overwriting of user files. You can use the [`-bd`](cmdlineref/build.md#-build-directory) (stands for `b`uild `d`irectory) command line option to specify it. We recommend using the `build` directory in your project working directory.

Without specify a build directory, your build tasks which produce file outputs will most likely fail.

<small>

Note: When designing the build system, we decided not to set an automatic build directory for the command line interface. It is to avoid accidental overwriting of other files that may be present for a given project. We wanted to avoid implicit overwriting of files in cases where you happen to use multiple build systems or tools for the same project. 

</small>
