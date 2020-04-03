# Source file

This article describes the top level constructs that can appear in a script file.

## Build target

The expressions of the language are enclosed in top level build targets. The build targets are the access points for external users to invoke parts of the build script. E.g. these get invoked when you start a build process.

```sakerscript
build {
	# you can declare your build expressions here
}
```

The build targets are not subject to any access control by the language. If you declare a build target, it will be callable from outside of the script. (See [`include()`](/doc/scripting/builtintasks/include.md)) To discourage calling internal build targets, we recommend prefixing them with the `'_'` character to signal that they're private.

Build targets are allowed to bear multiple names, in which case they serve as an alias.

```sakerscript
build, compile {
	# expressions...
}
```

In the above example, invoking either `build` or `compile` will result in the same execution of expressions.

### Parameters

The build targets can declare multiple input and output parameters. This facility can help reusability of build targets when they are executing commonly occurring tasks.

The parameters are declared in parentheses after the name (and aliases) of the build target. Any parameter are preceded by the type of it, which is either `in` for input and `out` for output parameters:

```sakerscript
build(
	in InputParam1 = 123,
	in InputParam2,
	
	out OutputParam1,
	out OutputParam2 = execute.some.task(),
) {
	$OutputParam1 = #...
}
```

The parameter list for a build target is optional.

#### Input parameters

Input parameters can be passed to the build target by external invokers. They may be required or optional which is based on if they have a default value declared for them or not.

The input parameters will be accessible by the expressions of a build target by dereferencing a variable with the same name. See [](/doc/scripting/langref/variables/index.md) for more information.

In the above example, `InputParam1` has the default value of `123`, and `InputParam2` is a required parameter. If a required parameter is not specified by the invoker, the calling of the build target will result in an exception.

The default value of `InputParam1` can be overridden by the caller, if they specify a value with the name of it.

#### Output parameters

Output parameters of a build target are considered to be the results of a target invocation. They may be arbitrary values specified by the semantics of the build target.

The output parameters must be assigned by the build target explicitly, by assigning a value to the variable with the same name. Example for this is `OutputParam1` in the above example. See [](/doc/scripting/langref/variables/index.md) for more information.

The output parameters which have values assigned to them in the parameter list (`OutputParam2` in the above example) will have that expression as their result, and may not be reassigned in the body of the build target.

If an output parameter is not assigned in the body of the target, the target execution will fail.

## Global expressions

The build files can contain global expressions. Global expressions are part of all build targets, and they will run when any of the build targets are invoked. Note that if none of the build targets are invoked in the script, the global expressions will **not** run at all.

```sakerscript
global.expression.task()
build {
	build.target.task()
}
```

In the above example, a build target and a single global expression is defined. When any of the build targets are invoked, both `global.expression.task()` and `build.target.task()` will be evaluated.

In global expressions, the same expressions can be used as inside build targets. See the expressions sections of the reference for more information.

If a script file contains only global expressions, but no build targets, then an implicit build target with the name of `build` will be declared for the file.