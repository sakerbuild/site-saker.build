[!section](intellijplugin.md)
[!section](eclipseplugin.md)
[!section](releasearchive.md)

# Installation

Saker.build has a single and only dependency on the Java Runtime Environment (JRE). In order to use it you first need to install it for your operating system of choice.

1. Install an appropriate Java Development Kit (version 8+)
2. [Download](https://api.nest.saker.build/bundle/download/saker.build-v#MACRO_VERSION_SAKER_BUILD) the saker.build distribution
3. Run `java -jar saker.build.jar`

You can also use command line tools to download a release with a specific version:
	
cURL:
```plaintext code-wrap
curl https://api.nest.saker.build/bundle/download/saker.build-v#MACRO_VERSION_SAKER_BUILD -o saker.build.jar
```

PowerShell: 
```plaintext code-wrap
Invoke-WebRequest "https://api.nest.saker.build/bundle/download/saker.build-v#MACRO_VERSION_SAKER_BUILD" -OutFile saker.build.jar
```

You can also use the following commands to download the latest version:

cURL:
```plaintext code-wrap
curl https://api.nest.saker.build/bundle/download/saker.build-v$(curl -s https://mirror.nest.saker.build/badges/saker.build/latest.txt) -o saker.build.jar
```

PowerShell: 
```plaintext code-wrap
Invoke-WebRequest "https://api.nest.saker.build/bundle/download/saker.build-v$((Invoke-WebRequest "https://mirror.nest.saker.build/badges/saker.build/latest.txt" -UseBasicParsing).Content)" -OutFile saker.build.jar
```

The saker.build system can also be added to the supported [IDEs](#ides) as plugins.

## Java

Saker.build requires the minimum JRE version of 8. You can download it from [AdoptOpenJDK.net](https://adoptopenjdk.net) or from the [Oracle Website](https://www.oracle.com/technetwork/java/javase/downloads/index.html). You can also use other installation mechanisms depending on your environment, but in the end, make sure to have a properly configured JVM installed on your machine.

When you're given the choice whether you want to install the JRE or JDK (Java Development Kit), we recommend choosing JDK, as that enables compiling Java sources and contains other development tools.

## Saker.build

The build system is distributed as a single self-contained Java Archive. You can download the latest release JAR file using [this link](https://api.nest.saker.build/bundle/download/saker.build-v#MACRO_VERSION_SAKER_BUILD).

You can also download older releases by visiting the [release archive](releasearchive.md). 

## IDEs

Saker.build support is available for the following Integrated Development Environment(s).

### IntelliJ IDEs

Saker.build system plugin is available for various JetBrains IDEs. The plugin is available on the [JetBrains plugin marketplace](https://plugins.jetbrains.com/plugin/14152-saker-build-system). Use the following steps to install:

1. Open the *File* > *Settings* dialog.
2. Select the **Plugins** page.
3. On the *Marketplace* tab, search for *saker.build*.
4. Select and install the *Saker.build system* plugin. 
5. Restart the IDE.

See the [](intellijplugin.md) section for alternative installations and more information.

### Eclipse

The steps to install the saker.build plugin for [Eclipse](https://www.eclipse.org/eclipseide/) are the following:

1. Open Eclipse and select the Help > Install New Software... menu.
2. Enter the `https://saker.build/saker.build.ide.eclipse/update-site/v#MACRO_VERSION_ECLIPSE_PLUGINS` link for the update site. ("Work with:" textbox)
3. Select the plugins and install them.
	* If you don't see the plugins, uncheck 'Group items by category'. (Eclipse [bugs](https://bugs.eclipse.org/bugs/show_bug.cgi?id=278673) may cause categories to not show up.)
4. See the [Eclipse plugin introduction](eclipseplugin.md) for getting started.
