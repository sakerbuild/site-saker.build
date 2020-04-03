# Do we need another build system?

<small>2020 January 6</small>

Let us begin with the infamous xkcd: Standards.

![Standards](res/xkcd_standards.png "Fortunately, the charging one has been solved now that we've all standardized on mini-USB. Or is it micro-USB? Shit.")

<small style="text-align: center;">

From [xkcd: Standards](https://xkcd.com/927/).

</small>

Basically *every* discussion on the internet about builds system reference the above xkcd sooner or later. Build systems are very often a heated or controversial topic, sometimes even more than build languages. Everyone has their preferences and opinions about build systems.

So the question is straight forward, *"Why, oh why do we need another build system?"*. 

The answer is however, not. If you've clicked on this article looking for the answer, we're sorry for tricking you. We're going to look at how saker.build became to exist, and how it provides something else and what differentiates it from other build systems.

### History

We've made a build system by accident. Okay, but let's not jump so forward.

In the beginning, we didn't want a build system. We wanted to execute a simple thing part of our build process. We had an Android hobby project back when it was still using Eclipse as its IDE. We needed a simple program that parsed some XML and spew out some C++ code for the project. Doing this was plain and simple. Implemented it in Java, and added it as an external build step for the project.

As time went on, we added more and more functionality to this build step, and the Java program grew quite a bit. The offline processing of the resources was much easier than doing it when the app was running. (We were using the Android NDK for the project, and resource processing in Java is easier than in C++.)

Later on, extending the build step became more and more tedious as the added features to it became interlaced and hard to maintain. So naturally we've modified it to load the different parts dynamically, have the components loosely coupled, and put an execution engine behind it. 

You can probably see where this is headin. After a few more iterations of the above, we've ended up with a little program that could perform the build by on its own. It was able to build the app for multiple platforms with multiple languages. (The Android app ended up to be a cross-platform app with C++.)

Between then and now, the program got rewritten a few times, and this is how we ended up with saker.build. However, we feel like there is still a question in the back of your mind.

**Why haven't we switched to using a build system?**

The first and most important reason is that we had no incentive to. The project was a hobby project, and we had *fun* building it in any way. Build-time resource processing is awesome, and we strongly encourage everyone to perform the operations they can during builds instead of runtime.

Secondly, we were deep in [tech dept](https://en.wikipedia.org/wiki/Technical_debt). Pulling in a new build system for the project and integrating the external build step into it didn't seem like a feasible thing to do. We had it working, and it worked the way we liked it. Throwing this state away for something that *may* work didn't make much sense.

Thirdly, we haven't found a build system that supported the platforms we've targeted. The associated Android project ended up to be a multi-platform C++ based project that supported Android, iOS, macOS, UWP, Win32, and Linux. We've wanted a single tool to build our project and we've had/made it.

### Technicalities

In the last paragraph of the previous section we've arrived what was the core requirement for the build system that we wanted to use and ended up with. *To be a single tool that manages the build process.*

Well, all build systems do that don't they? No. There are lot of build systems that require integration with other tools, like [meta-build systems](https://en.wikipedia.org/wiki/List_of_build_automation_software#Meta-build) and [build script generators](https://en.wikipedia.org/wiki/List_of_build_automation_software#Build_script_generation). Some build systems doesn't support modern features like build clusters and build cache. But even if they do, they may usually require you to download and use additional programs alongside your build system.

## Conclusion

We've seen that there's no (or we haven't found one yet) build system available for developers that provide built-in support for all modern build features. This include incremental builds, scalability, extensibility, build caching, build clusters, and others. Saker.build aims to work as an all-in-one build system that doesn't need other tools to be downloaded in order to provide all the mentioned features.

While the above doesn't answer why we need *another* build system, we hope it answers why saker.build has a reason to exist.
