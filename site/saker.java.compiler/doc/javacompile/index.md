# Overview

The [`saker.java.compile()`](/taskdoc/saker.java.compile.html) build task for the saker.build system performs incremental Java source compilation and annotation processing. It serves as a frontend task to the `javac` tool.

The main features of the task is that it always recompiles only the minimal amount of source files necessary to keep the compilation result up to date. It also performs the annotation processing in an incremental way, and provides additional configuration options for the compilation.

The following documents serves as a guide for using the [`saker.java.compile()`](/taskdoc/saker.java.compile.html) build task.
