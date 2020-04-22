# IntelliJ plugin for saker.build v0.8.11

<small>2020 April 22</small>

We've been hard at work to implement plugins for the IntelliJ IDEs that support saker.build. The first release is finally out! See the [installation instructions](root:/saker.build/doc/intellijplugin.html) to get started!

The plugin serves as the core for the saker.build support in IntelliJ IDEs. It allows configuring your project to properly run your builds, and helps you write the build scripts with completion proposals, syntax highlight, and structural outline.

The current implementation of the plugin mostly reached feature parity with the [Eclipse plugin](root:/saker.build/doc/eclipseplugin.html), some aspects of it is still being ironed out. (E.g. configuring other languages support based on the build configuration is still missing)

We've also improved the build script editor support in this version, which applies to the newly released Eclipse plugin as well. The code completion proposals should provide better results, and be more error-tolerant if the script contains syntax errors.

![Code completion example GIF](raw://res/gfx/code_completion.gif)

While the dark theme for Eclipse is rarely used, it is more dominant in the JetBrains land. We've tweaked the build script syntax highlight colors for the dark style in order to be more readable.

![Syntax highlight example](raw://res/gfx/syntax_highlight_dual.png)

### What's next?

We've started developing the IntelliJ plugin not only because its a widely used IDE, but because we're also developing [Android support](https://github.com/sakerbuild/saker.android) for saker.build. You'll probably want to use Android Studio alongside that, so supporting IntelliJ is a first step for supporting Android Studio as well.\
(Note that you can install the just released plugin for Android Studio, but it won't have any specific Android related support yet.)

In the following times we'll be looking at releasing the Android support package for saker.build, while creating additional plugins for the IntelliJ ecosystem in order to properly configure the IDE based on the build configuration.
