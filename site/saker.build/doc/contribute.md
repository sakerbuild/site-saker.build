[!section](buildstatus.md)

# Contribute

We're thrilled to have you here! Contributions for saker.build and related ecosystem is always welcome. Let's see how you can help.

## Report issues

If you encounter unexpected behaviour, bugs, missing documentation, typos or anything else that is out of the ordinary, you should file a bug report for it. This serves as the primary feedback for developing saker.build and can greatly help us to determine the future features to implement and prioritize bugs based on them.

To report an issue, follow these steps:

1. Verify that the issue you're about to report is something that shouldn't work that way.
	* Check the documentations of the build system, build tasks, and others that you're using, and verify that it is indeed the underlying software is at fault.
2. Determine the software component that is associated with the issue.
	* The issues should be reported to the maintainers of a given component.
	* For saker.build build system related issues, report it at [saker.build GitHub Issue tracker]( https://github.com/sakerbuild/saker.build/issues/).
	* For other build tasks, consult the author of the given build task, and report it for the maintainers.
		* For example, issues with the [`saker.java.compile()`](root:/saker.java.compiler/taskdoc/saker.java.compile.html) should be reported at [Saker.java.compiler GitHub Issue tracker](https://github.com/sakerbuild/saker.java.compiler/issues/).
	* If we receive issue reports for different components than the one it was reported for, the issues will be moved to that associated issue trackers.
3. Choose an appropriate issue template, fill it, and submit.
4. Help the maintainers following up on the issue by producing a sample project that reproduces the issue. Actively communicate with the team when appropriate.

## Develop build tasks

You can create new build tasks that can be integrated with the saker.build system. Saker.build has a public API that you can develop your plugins against. You can get started by visiting the [task development guide](/doc/extending/taskdev/index.md).

If you want to publish the created build tasks, we recommend uploading them to the [saker.nest repository](https://nest.saker.build) so other developers can use them as well.

## Code contributions

You can contribute code to the projects that are managed by the saker.build mantainers. This can include bugfixes, feature implementations, and other additions that improve the build system and related projects.

First of all, file an issue that will serve as a tracker for your progress and discussion about the implementation. It is used to consult with the maintainers in order to have a clear understanding of what you're going to implement and in which way we can accept it. Before writing any code, make sure to propose your plans with us so we won't reject it, and your efforts won't go out the window.

The code will be reviewed and accepted via a [pull request](https://help.github.com/en/github/collaborating-with-issues-and-pull-requests/about-pull-requests).

<div class="doc-wip">

The code contribution guide is still work in progress. We're still determining the full legal aspects of accepting contributions. You're most likely will be needing to sign a [CLA](https://en.wikipedia.org/wiki/Contributor_License_Agreement).

</div>

