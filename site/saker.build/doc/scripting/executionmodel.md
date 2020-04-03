# Execution model

SakerScript defines an eager, data-driven, nonlinear execution model. It evaluates all expression at the same time, while waiting for uncompleted ones when necessary. The expressions can be declared out of order, it won't change the flow.

## Data-driven

The data-driven execution model means that the evaluation of expressions is based on the available data. In order to understand this, let's look at some other models.

The imperative execution model is the most common one, this is how common CPUs and programming languages work. You write your program code, and the interpreter (the CPU in most cases) will evaluate it step by step, after one another. Such programming languages are easy to understand, as you can easily determine the control flow of the program, just by looking at the subsequent expressions.

The demand-driven execution model is another one, which is based on the requested data and its dependencies. In this model, expressions are only evaluated when there's a need for its results. We consider this model to not be suitable for a build system language, as not explicitly using the output of a task can result in it being omitted.

When a data-driven execution model is used the expressions are evaluated, and when they complete, the dependent expressions are started to evaluate as well. This model provides the opportunity to greatly parallelize the execution while evaluating every expression that is required.

```sakerscript
build(
	out Output
) {
	$Output = output.task()
	other.task()
}
```

Let's look at the above example with the respect of possible execution models.

**In case of imperative model**, we evaluate `output.task()`, assign it to `$Output`, and run `other.task()`. This produces the correct output as we expect both tasks to run, but provides no parallelization, therefore leaving computation resources unused, hindering performance.

**In case of demand-driven model**, we see that the build target only has the `Output` as its output. We can determine, that only `output.task()` is part of the result set, therefore the execution model can ignore the invocation of `other.task()`. This provides incorrect results, as we expect both tasks to run, but the call to `other.task()` were omitted, due to the fact that it is not required to produce the output.\
This execution model provides great parallelization, but limits proper usability.

**In case of data-driven model**, we start `output.task()` and `other.task()` at the same time, and when the result of `output.task()` is ready, we assign it to `$Output`. This model maxes out parallelization, and provides correct outputs, as all tasks are executed properly.

Note that in this case the data-driven model is roughly the same as the demand-driven, but we simply don't omit the calls to tasks which aren't part of the output.

Another advantage of this model is that in the above example we can return the result of the build target early, while `other.task()` is still executing. The caller can start using the result as soon as it's ready, and there is no need for a synchronization point in a build target execution.

<small>

(Note that there are more formal definitions of the mentioned execution models and we hope we captured the gist of them in a nutshell.)

</small>

## Eagerness

The evaluation of expressions is eager, meaning that *all* expressions are evaluated concurrently and they will be stalled when they attempt to use the result of another expression. This means that its the responsibility of the expressions to retrieve the results of their dependencies, not the language execution model to only evaluate an expression if all of the inputs are available.

This results in the advantage that if an expression doesn't use an input of its, then it doesn't need to wait for the evaluation of it. We can see this in the following example:

```sakerscript
build(
	out Output
) {
	$Output = include(second, Input: $Unassigned)[SecondOut]
}
second(
	in Input,
	out SecondOut,
) {
	$SecondOut = second.task()
}
```

We run the `build` build target, which produces its `Output` based on the output of the `second` build target that it invoked using `include()`. The `Input` parameter with the variable value of `$Unassigned` is passed to the `second` build target. The variable `$Unassigned` is never assigned during execution.

We can see that the `second` build target never uses its `Input` parameter, therefore the value of `$Unassigned` is never retrieved. If the value of `$Unassigned` was used, then the execution of the build target would deadlock, and fail. However, as the responsibility of retrieving the results of the dependencies of an expression has been assigned to the expressions instead of the execution model, this allows us to successfully complete the execution of the above example.

## Nonlinearity

As previously mentioned, the expressions declared in the language are not evaluated in the order they were declared in. This means that changing the order of expressions will yield the same results, irregardless of the order.

```sakerscript
build {
	$firstOut = first.task()
	second.task($firstOut)
}
```

The above will produce the same results as the following:

```sakerscript
build {
	second.task($firstOut)
	$firstOut = first.task()
}
```

This is a big advantage of the language. It provides great parallelization of the evaluation, while maintaining the readability of the code. You're able to write code that follows the logic of linear progression, while parallelizing the expressions automatically based on the data dependencies.

This basically makes SakerScript a declarative language, in the sense that the execution flow depends on the data availability rather then the order of the expressions. We believe that this solution is most applicable for a build language.

(The task invocations in the above example can be replaced with any other more complex expressions.)

Please note that as while we claim that the code can follow the logic of linear progression, it is the responsibility of the developer to write such code. We recommend not to spread snippets of code in random places of the build file but to write consumer code of the produced data as close as possible to ensure proper readability.