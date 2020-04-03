# Basics

The Microsoft Visual C++ toolchain provides features for C/C++ source compilation and linking. It can produce executables, dynamic libraries, and others.

The saker.msvc package provides access to the functionalities provided by the MSVC toolchain. The [`saker.msvc.ccompile()`](/taskdoc/saker.msvc.ccompile.html) task compiles C/C++ source files, and the [`saker.msvc.clink()`](/taskdoc/saker.msvc.clink.html) task links object files (among other inputs) into an executable or dynamic library.

The tasks support using build clusters and therefore distributing the compilation tasks to a network of machines. The tasks also allow not having a local toolchain installation, and dispatching all work to the clusters.
