# API structure

The main and single access point of the scripting API of the build system is the `ScriptAccessProvider` interface. This interface should be implemented by script language developers, as an instance of it will be loaded by the build system to access scripting related functionality.

See [](packaging.md) for information about how it is loaded by the build system.

## Target configuration reader

The [`TargetConfigurationReader`](/javadoc/saker/build/scripting/TargetConfigurationReader.html) interface is simply responsible for parsing a build script and creating the build target task instances that can be invoked during a build. The method `readConfiguration` has the arguments of the byte input stream of the build script and the script parsing options which can be used for additional configuration of the parser.

The [`ScriptParsingOptions`](/javadoc/saker/build/scripting/ScriptParsingOptions.html) interface provides the information about the currently parsed build script. Developers can retrieve the path of the script file, and the user provided parsing options for it.

The options are string key-value pairs which can be defined by the user for a given script. They can be interpreted arbitrarily by the parsing implementation. See [](/doc/guide/scriptlanguages.md) for more information.

### Configuration reading result

As the result of the parsing of the script file, a [`TargetConfigurationReadingResult`](/javadoc/saker/build/scripting/TargetConfigurationReadingResult.html) instance must be returned. It provides access to the parsed configuration, and as well to an information provider object which can be used to retrieve positional information about source code elements.

The [`TargetConfiguration`](/javadoc/saker/build/runtime/execution/TargetConfiguration.html) will be used by the build system to retrieve the task factory instances that are invoked during a build execution. \
The [`ScriptInformationProvider`](/javadoc/saker/build/scripting/ScriptInformationProvider.html) is mainly used to determine the origin of an exception when a task execution fails. The build system will attempt to create a *script trace* that displays what task invocations lead to a given exception. Implementing an information provider is optional, but recommended.

## Modelling engine

The modelling engine ([`ScriptModellingEngine`](/javadoc/saker/build/scripting/model/ScriptModellingEngine.html)) is the access point for Integrated Development Environments (IDE) to use when the user attempts to view/edit a file associated with the given scripting language. When supported, IDE plugins use this engine to create models of a given script file and provide support features for the user to view and edit the given file.

Modelling engines are attached to a given modelling environment which provides access to the other script models and the configuration of the environment.

The modelling engine implementations are responsible for creating model objects that provide assistant features for the user. These features include:

* Semantic code highlight
	* Also called *syntax highlight*.
	* Determines the styles of different parts of the script. It can be used to visually differentiate various elements in the source code based on their semantic interpretation. 
	* E.g. constants, variables, build targets can be all styled differently so the user will have a visual indication about the part of the code and what it stands for.
* Content assist
	* Also called *code completion*, *auto-complete*, *IntelliSense*.
	* When the user invokes the content assistant, the script models will be queried about possible completions that could be inserted into the code. It is a list of modifications in the source code with possible additional information about their purpose.
	* E.g. when the user invokes content assist on `$va`, the script model can suggest to complete the phrase to an existing name of `$variable`.
* Documentation access
	* When the user explicitly requests information about a given source code element, the script models can look up and provide documentational or other various information about it.
	* E.g. displaying the documentation of a task when the user hovers their cursor over a task declaration.
* Structural outline
	* The models may provide structural outline features which are usually represented in a tree-like view in the IDE. It can enhance navigational information about the source code and may enable the user to find various elements in the code faster.
	* E.g. the feature may list the build targets in a script, and the task invocations in it.

All of the above features are optional to the script language implementation, and the IDE editors must work without implementing them. Language developers can gradually implement such features, as it can significantly enhance user experience.

## Accessor key

Each script language implementation needs to be uniquely identified. This is in order for the build system to properly implement class lookup, and handle the case when the scripting language implementation changes.

The [script accessor key](/javadoc/saker/build/scripting/ScriptAccessProvider.html#getScriptAccessorKey--) is responsible for uniquely identifying the script implementation which is a plain Java object that should implement the `equals`, `hashCode` and `toString` methods appropriately to ensure that it uniquely identifies the implementation version. 

This object is recommended to be an instance of a custom data class that is part of the language implementation and implements the mentioned methods appropriately.
