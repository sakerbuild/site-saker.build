# "Do I need a build system?"

<small>2020 January 27</small>

A question for which the answer is always **no**.

That sounds strange from a blog of a build system. To be clear, we're not saying you shouldn't use a build system, but only that the answer to the above question is always *no*. In general, you can think of the question as *"Do I need X?"*, and we'll examine it in regard to the development of software projects.

The concept of engineering is the process of finding a solution for a given problem. This applies to many fields as well as to developing software. Everything you do when you create an application should help you achieve the goal of solving the initial problem.

It's important to keep in mind during development that with every introduced component to the project, you also introduce complexity. By adding new tools, infrastructure, services, you may need to keep these parts of the project in mind when proceeding further.

There will be a point in time where you'll need set the scope of your project and stop introducing new components. At this point, you'll set the requirements for your project, choose the components that satisfy them and help you develop the solution for your problem. You won't be asking *"Do I need X?"* but will start asking *"What requirements does X satisfy?"*. If the answer is not an empty set, then you have a chance that using *X* will be useful for developing your solution.  

### It's all backwards

We've seen that the process of engineering presumes an initial problem. The interesting thing about the question *"Do I need a build system?"* is that it's all backwards.

During engineering, we have a problem, and search for a solution. When asking *"Do I need a build system?"*, **we have a solution, and search for a problem**. By asking this, we search for a reason to add a build system, although it doesn't provide any actual benefit for us other than being there.

And this is were we can conclude that the answer for the question is always **no**, as if an addition to the project doesn't further its goal, there's no reason to perform it. Asking the question *"Do I need X?"* displays an intent for using a given technology rather than solving the initial problem.

Solve problems with the appropriate tools, don't add more problems in order to use a tool.

##### *But what if I'll need it later?*

You may need a build system later as the project advances. If that happens, you should deal with it then rather when you don't actually need it. Maintaining a build system for a project when you don't even benefit from it just increases complexity rather than add value to it.

It may also take less effort to integrate it at a later time rather than add it sooner and maintain it on the way.

### Examples

Without any claim to completeness, here are a few examples when you would benefit or not from using a build system:

##### Cons of using a build system

* A hobby project that can be compiled in an IDE.
	* E.g. a simple Java project in Eclipse, or project in Visual Studio.
* No other contributors.
	* If you develop alone, you may be fine without a build system as an uniform build process is not a requirement.
* No dependencies.
	* If you don't consume third party components.
* Static dependencies.
	* If you don't need to auto-upgrade dependencies, you can just simply download and use them without the need of actively managing them.
* Simple build process.
	* If you just simply want to create an executable, but don't write any tests or perform static verifications.

You may think that the above points only apply to a very small number of projects, and you're right. Build systems are awesome, and provide many benefits for managing a software development process. Most production grade projects are usually set up in a more complex way. However, if you can get by without using one, it can spare you some trouble.

##### Benefits of a build system

* Uniform build process.
	* The build should run the same way on any machine of the contributors. A build system can be used to properly set up the development environment for the programmers.
* Testing, static verification.
	* A build system can manage the testing and verification of your software before you release a new version.
* Managing dependencies.
	* Build systems can automatically download, upgrade, package, and otherwise manage the dependencies you include in your project.
* Incremental builds.
	* For large projects it's important to keep the build times as short as possible. Incrementally building a project based on the previous results can result in much quicker modify-build-run cycle.
* Distributed builds.
	* As your project gets larger, build times increase as well. Using multiple computers can reduce the build times significantly.
* Build-time resource generation.
	* Generating source files or other resources during the build process can make many tasks easier. (E.g. accessing resources, communication between components, logging, etc...)
* Caching.
	* A build cache can store the outputs of your build so you don't have to perform a clean build when you check out the codebase.
* Education.
	* Learning new tools can be great to extend your toolbox.

## Conclusion

Using a build system to build your project is great when you have an use-case for it. However, you should always keep in mind that adding it to your project nearly always increases its complexity. If you're just messing around, the maintenance burden may outweigh the benefits, however, for production grade projects, using one can greatly improve the development process.
