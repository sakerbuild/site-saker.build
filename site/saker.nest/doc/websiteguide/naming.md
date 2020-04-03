# Naming

Both package and task names in the repository must be bound to a given user before uploading any bundles related to them. Name allocation is required so different users won't publish different content under the same name alias.

When packages are created, the package name must be allocated. It can be done so by simply creating the package itself.

Task name allocation differs than package names, as they are allocated after their enclosing package is created. When you attempt to upload a bundle that contains a build task with a given name, you must first claim the given task name for your package. Not doing so will cause the repository to produce a validation error for that given bundle.

## Name formats

Both package names and task names have the same dot separated identifier format. Saker.nest requires the package and task names to contain **at least two** identifier components. This is in order to avoid confusion about the source of the content, and to require differentation of similar content. (Single component task names may also be reserved by the build system script implementations.)

We recommend for package names to use the following semantics:

```plaintext
[company.name].[package.purpose]
org.cool.company.file.utils
org.super.company.lang.compiler
```

The package name that contains the company name and the purpose of the package can be easily used to uniquely identify it and its contents. Another advantage is that if others decide to publish a package with similar purpose, the preceeding company name will distinguish different implementations.

For task names we recommend to convey information about what the task does, and also prefixing the task name with a short company identifier:

```sakerscript
cool.file.copy()
super.lang.compile()
```

The tasks shouldn't have any `org`, `com` or similar common domain prefixes part of their names, as they don't convey important information in relation to the tasks themselves.

Similar to the Saker task names provided by us:

```sakerscript
saker.java.compile()
saker.jar.create()
```

Claiming a task name can happen any time after you create a package, however, if you've published at least one bundle that contains a task for the given name, that task name cannot be unclaimed.

## Restricted names

In order to avoid ambiguities, impersonation, and to build trust, we've decided to restrict the usage of some common names in task and package names. This is in order to avoid confusion about the contents of a package, their authors and the validity of the resources.

Various names that may be commonly used or represent well-known actors in the community may only be allocated by verified users. For example if someone wants to allocate a package name of `apple.swift.compiler`, then that name allocation will fail, unless the user have manually authenticated themselves to be an Apple representative. This is in order to ensure that if someone sees a package that starts with `apple.`, then they can be sure that it's authentic Apple provided package, rather than some other (possibly malicious) member of the community.

Following up on the above example, if someone wants to publish a package that serves similar purposes, they can do so under their own `mycompany.swift.compiler` package name.

The restrictions mainly apply to the first component of the allocated package and task names, however, in case of abuse we reserve the right to extend this to other aspects of the allocated names as well.

The restricted names are mainly names of common actors in the software domain, commonly used operations and methods, various language names, protocol names, format extensions, and other commonly used technical phrases.

If you want to use a given restricted name, [contact us](raw://contact.html) via e-mail describing your intent and use-case.
