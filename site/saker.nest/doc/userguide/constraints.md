# Constraint properties

The repository allows defining constraint properties which are used to modify the bundle loading behaviour of the repository in some cases. Bundles are allowed to define constraints on themselves to limit how and in which cases can they be loaded by the repository.

The following mostly apply to loading the class path for the bundles, but are applicable in other cases as well. The best way to explain this is by looking at the following example:

Given the scenario, when the repository runtime is loaded in a Java Runtime Environment with a major release version of 8. The bundle `example.bundle` is requested to be loaded by the repository. However, the `example.bundle` declares a constraint, that it can be only loaded on JRE version 9 or later. In this case, the loading of `example.bundle` will *fail*.

In other words, constraints are prerequsites specified on the enclosing environment. It can be used to load different bundles for different environments.

The following constraints are used by saker.nest.

## JRE major version

Bundles can specify a constraint to be only loaded if the current Java environment major version number matches the one specified by them.

The [](configuration.md#constraint_force_jre_major) repository parameter can be used to override the default.

## Repository version

Constraint that applies to the current full release number of the saker.nest repository.

The [](configuration.md#constraint_force_repo_version) repository parameter can be used to override the default.

## Saker.build system version

Constraint that applies to the current full release number of the saker.build system environment that loaded the runtime.

The [](configuration.md#constraint_force_buildsystem_version) repository parameter can be used to override the default.

## Native architecture

Constraint that applies to the underlying native architecture of the runtime. The value of this constraint is based on the `"os.arch"` Java system property.

The [](configuration.md#constraint_force_architecture) repository parameter can be used to override the default.
