# Task workflow

Tasks implementations are strongly recommended to follow the workflow that is described in this article. It is defined in order to keep the deterministic behaviour of task implementations and for the build system to be able to implement an efficient and valid build delta detection.

The build system defines the execution model of the tasks to be side-effect free pure functions that produces their outputs based solely on their inputs. As tasks run in the build system concurrently and share common resources with each other, they will need to follow a specific, but straightforward workflow that ensures that the information flow between the tasks are ordered properly.  

## The workflow

Task implementations should adhere to the following workflow:

1. Wait for the input task dependencies of the task if any.
	* This means that any operations that wait for other task executions to complete need to happen before the following point.
	* This relates to the functions described in [](retrievingtaskresults.md).
	* **Important**: Tasks shouldn't access any files in the build system or any file-related deltas before waiting for the input tasks.
2. Access any files necessary for the task to complete, and execute its operations.
	* After waiting for the input tasks, the task implementation can access the input files for it.
	* Any other operations the task needs to to can be done in this phase.

The above requirements are justified by seeing the following aspects:

* The filesystem is a shared resource, and the build system is a highly concurrent one. If you depend on a task, then you also probably need to acces the files it has produced. 
	* In order to do that, you need to wait for the input task to finish, because if you don't, then the input task might not have created the files that you need as your input, therefore you won't find the files you're looking for. However, if you wait for the task, then you'll find the files, and can complete your operations accordingly.
	* It is considered to be an invalid task implementation that tries to access an output file of a task before waiting for the associated task to finish.
* The build system cannot make assumptions on how the task implementation will operate. It could decide to do completely different things based on different task results, or file contents.
	* In order to properly calculate the incremental deltas for the task, the build system needs to ensure that the filesystem is in the state that the task would perceive. In order to reach this state, it needs to wait the input task dependencies, and then it can check if any of the files have been changed.

We can see that the defined workflow is a common ground between the task implementations and the build system in order to provide an efficient build execution. This helps the build to be deterministic, and also encourages the task implementations to separate the concerns of determining the configuration and executing its operations. 

Typically, when you implement a task, you don't need to worry about this too much, as the general workflow of the task are already adhere to this. Usually you initialize the parameters of your task, determine the configuration based on the parameters, and then do your work based on that. During determining the configurations, you won't access any files, which is in line with the above workflow. If you do, make sure to do it after accessing any parameters of your task.

## Violating the workflow

Don't do it. It will most likely result in incorrect builds, and you probably don't have any incentive to break the workflow. However, even for the best intentions, it is possible to accidentally violate the above workflow. In order to avoid this, we recommend the following:

1. If your task is parameterized, retrieve the parameters of your task as your first step of execution.
	* When retrieving the results of the parameters, don't just get the first direct objects of a task result, but get all important aspect of it.
2. We recommend to build your own configuration model based on the parameters. This will ensure that all necessary data is available, and you won't have to wait for tasks during your execution. (Explicitly or implicitly)
3. When dealing with [structured task results](taskresults.md#structured-results), make sure to retrieve all fields or elements of it instead of just the task identifiers.
4. After all configuration has been retrieved, you can freely access the files and other necessary resources of the build system.

**Important**: Don't wait on different tasks based on the contents of a file. This is a strong violation of the above workflow, and the input tasks of your task must not depend on the contents of a file.

Note that waiting on a task that you've started will not violate the above workflow. You can wait for subtasks even after you've accessed the interested files.
