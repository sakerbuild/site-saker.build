# Basics

The clang tool lets you compile C, C++, Objective-C, Objective-C++ source files and link them into an executable or library. This operation is separated into two steps called compilation and linking.

The [`saker.clang.compile()`](/taskdoc/index.html) task lets you compile the source files which produce object files as the output. These objects files then later can be linked together using the [`saker.clang.link()`](/taskdoc/saker.clang.link.html) task.

These tasks support using build clusters and therefore distribution the compilation and linking operations onto multiple build machines.
