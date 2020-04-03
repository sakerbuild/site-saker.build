# API structure

In order to implement a repository, you need to implement the [`SakerRepository`](/javadoc/saker/build/runtime/repository/SakerRepository.html) interface and make it available with a [`SakerRepositoryFactory`](/javadoc/saker/build/runtime/repository/SakerRepositoryFactory.html) implementation. 

## `SakerRepositoryFactory`

The factory interface is a stateless class that is necessary for creating the repository instances. It is the entry point for creating a repository instance. It may be used via the `ServiceLoader` facility, or instantiated in other ways. The implementations must have a public no-arg constructor.

In general, the implementation for your factory will be as simple as the following:

```java
public class MyRepositoryFactory implements SakerRepositoryFactory {
	@Override
	public SakerRepository create(RepositoryEnvironment environment) {
		return new MyRepository(environment);
	}
}
```

The necessity of differentating the two interfaces lies with the fact that the [`SakerRepository`](/javadoc/saker/build/runtime/repository/SakerRepository.html) class is stateful, therefore there needs to be a facility that is able to create instances of it.

## `SakerRepository`

The [`SakerRepository`](/javadoc/saker/build/runtime/repository/SakerRepository.html) interface is the main access point for any repository related features. It is instantiated by the associated [`SakerRepositoryFactory`](/javadoc/saker/build/runtime/repository/SakerRepositoryFactory.html), and closed accordingly if the build system no longer needs it.

The [`SakerRepository`](/javadoc/saker/build/runtime/repository/SakerRepository.html) instance has access to a [`RepositoryEnvironment`](/javadoc/saker/build/runtime/repository/RepositoryEnvironment.html) which provides access to various information and file system locations that are useable by the repository. The repositories can be instantiated without any build related configuration, and they may be shared between different build executions.

As a baseline, the repository implementations must implement the [`createBuildRepository`](/javadoc/saker/build/runtime/repository/SakerRepository.html#createBuildRepository-saker.build.runtime.repository.RepositoryBuildEnvironment-) method that creates a repository object for usage with a build execution. The returned [`BuildRepository`](/javadoc/saker/build/runtime/repository/BuildRepository.html) instance is private to a given build execution and may be configured by the user accordingly.

## `BuildRepository`

The [`BuildRepository`](/javadoc/saker/build/runtime/repository/BuildRepository.html) interface is a configured view for its enclosing [`SakerRepository`](/javadoc/saker/build/runtime/repository/SakerRepository.html). The main responsibility of it is to look up tasks given an user-provided [`TaskName`](/javadoc/saker/build/task/TaskName.html).

Build repositories may also be cached by a build daemon or other build system facilities, in which case they will not be reinstantiated for incremental builds, but reused. They will be given an opportunity to detect any changes that occurred to the state of a repository.

Build repositories may expose an information provider interface ([`ExternalScriptInformationProvider`](/javadoc/saker/build/scripting/model/info/ExternalScriptInformationProvider.html)) with which they can provide basic assistant features for script languages. Implementing such an interface is beneficial as it can greatly improve the user experience with the repository in an IDE editor.

See the JavaDoc for [`BuildRepository`](/javadoc/saker/build/runtime/repository/BuildRepository.html) for more information. 
