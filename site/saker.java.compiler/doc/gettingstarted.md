# Getting started

The saker.java.compiler package runs as part of the saker.build system. The packages are distributed using the saker.nest repository. In order to install them, see the installation guides for [saker.build](root:/saker.build/doc/installation.html) and [saker.nest](root:/saker.nest/doc/installation.html). 

Apart from the build system and repository runtime, the only prerequisite for the saker.java.compiler packages is that you use a JDK for running the builds. Saker.java.compiler has been tested to work with JDKs starting from the version 8.

To ensure that you have the JDK installed, you can use the following command:

```plaintext
javac -version
```

If it prints out a version rather than an error, you're good to go. If not, then we recommend installing a JDK from [AdoptOpenJDK](https://adoptopenjdk.net/).

If you're using saker.build inside an IDE, make sure the IDE runs on a JDK distribution.

After doing all that, you can go ahead and get started with some [examples](examples/index.md). If you're feeling adventurous, you can dive straight into with writing the build script, as the content assistant features should provide you with basic documentation about the [`saker.java.compile()`](/taskdoc/saker.java.compile.html) task.