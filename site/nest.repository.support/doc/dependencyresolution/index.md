# Overview

The package allows managing and resolving dependencies. The [`nest.dependency.resolve()`](/taskdoc/nest.dependency.resolve.html) task allows resolving the specified dependencies based on the specified rules passed as a parameter to the task.

The result of the dependency resolution task can be passed as an input to various other tasks that allow operating on them. The results are most often used to download the resolved bundles, or pass them as an input to further compilation or build tasks.
