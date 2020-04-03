# Parameter parsing

<div class="doc-wip">

Some aspects of this page is still under development. The conversion mechanics in this document may be incomplete or subject to change. See also: [DataConverterUtils](/javadoc/saker/build/util/data/DataConverterUtils.html).

</div>

When developing tasks, you probably want them to be parameterizable from the build script. In order to achieve this, your `Task` implementation needs to implement the `ParameterizableTask` interface, and you can override the `initParameters` method to handle the user provided parameters.

The saker.build system provides the default functionality for converting the user provided parameters and assigning them to the appropriate fields of your `Task` object. In order to use this, you need to annotate your fields with [`@SakerInput`](/javadoc/saker/build/task/utils/annot/SakerInput.html) and related annotations.

A simplest example for the above is the following:

```java
@SakerInput
public String MyValue;
```

Given that the above field is declared in your `ParameterizableTask` object, it can be used the following way:

```sakerscript
example.task(MyValue: abcd123)
```

This way the `MyValue` field will have the `"abcd123"` value when the `run(TaskContext)` method is called.

## Conversion mechanics

The exact conversion mechanics work on a best effort basis. The type converter will try to convert the value of a parameter to the target type by examining both objects and finding a conversion method for proper interpretation. The class [`DataConverterUtils`](/javadoc/saker/build/util/data/DataConverterUtils.html) is used to convert the types and you can read more information about it in the Javadoc.

In general, it is strongly recommended to use only interfaces or primitive types and their compositions on lists and maps for the parameter types. The build system will attempt to convert the values and will throw an exception if it failed. It is strongly recommended to test the possible input values and properly document the types a task parameter accepts.

If a task needs to specially handle some input parameter, override the `initParameters` function and act accordingly.

## `@DataContext`

The [`@DataContext`](/javadoc/saker/build/task/utils/annot/DataContext.html) annotation can be used to encapsulate a set of parameters into a custom class. This can be beneficial if the parameters will be reused in other places. The parsing facility will assign the annotated fields of the data context object accordingly.

```java
public class MyParameters {
	@SakerInput
	public String MyValue;
}

@DataContext
public MyParameters params;
```

In the above example the `MyValue` parameter of the `params` data context will be assigned the same way as if it was declared directly in the task object.

The `@DataContext` annotated fields will be recursively visited by the implementation. If the data context field is `null`, the implementation will instantiate the data context type using the no-arg constructor.

## Examples

The following will provide some examples and guidelines about how the parameters should be declared for a task.

### Primitive types

If the target type of the task parameter is a primitive of the Java language (also including `String`), the build system will use the default parsing methods for assigning the parameter. (E.g. `Integer.parseInt` and others)

Boxed primitive types work the same way.

### Types with `valueOf`

If the parameter field has a type that declares at least one `static` `valueOf` method with a single parameter, the build system will attempt to use that method to convert the value to the given type. An example for this is `SakerPath`, where the `valueOf(String)` is used to convert the value.

```java
@SakerInput
public SakerPath PathParam;
```

```sakerscript
example.task(PathParam: res/images)
```

Will result in the `PathParam` be assigned to a `SakerPath` instance representing `res/images`.

The `valueOf` methods are considered for conversion even if the target type is an interface.

### Lists and Maps

The parameter type can be declared to be Java Collection interfaces. We strongly recommend to only use the `List` and `Map`, but using `Collection` or `Iterable` can be also acceptable. `Set` and other collections that provide guarantees about the enclosed elements should **not** be used. It should also be noted that using `Map` as the parameter type might result in keys that occur multiple times and no guarantees are made about the uniqueness of the keys. Developers should iterate over the entries in the map rather than looking up the values for a given key.

These interfaces should always be used with type parameters, and their declaration should not be raw. (I.e. use `List<MyElementType>` rather than plain `List`)

When a value is being converted to a collection type, the build system will attempt to convert each element to the target type at an appropriate time. This can happen right away when the task parameters are assigned, or lazily when the elements are actually accessed.

If the build system encounters a non-collection value for the given target, then it will attempt to enclose it into a singleton collection when appropriate.

```java
@SakerInput
public List<Integer> IntsParam;
``` 

```sakerscript
example.task(IntsParam: 123)
example.task(IntsParam: [1, 2, 3])
```

Both of the above parameterization of the tasks are valid, in the first case the `IntsParam` will have a value of `[123]`, while in the second case it will have `[1, 2, 3]` accordingly.

Maps can be used similarly, but be noted that the key of the declared type should only be `String` or plain `Object`. Any other types may cause the map to work incorrectly.

```java
@SakerInput
public Map<String, Integer> StringIntsParam;
```

```sakerscript
example.task(StringIntsParam: {
	First: 1,
	Second: 2,
})
```

In the above example the value of `StringIntsParam` will be the same as it was specified in the build script.

All collection and map instances assigned by the build system are immutable.

### Enums

The parameters of a task can be declared as `enum` types. The build system will convert them by looking up the enum value with the corresponding name.

```java
public enum MyEnum {
	VAL1, VAL2;
}
@SakerInput
public MyEnum EnumParam;
```

```sakerscript
example.task(EnumParam: VAL2)
```

The parameter field will be correctly assigned with the enum instance of `VAL2`.

### Interfaces

The task parameter types may be `interface` declarations which the build system will automatically implement based on the parameter value. This is usually advantageous when the parameter is expected to be a map, but the user code wants to handle it in a convenient way.

```java
public interface MyParamType {
	public String getValue();
	
	public int getNumber();
	
	public default Double getFloating() {
		return 1.0;
	}
}
@SakerInput
public MyParamType MyParam;
```

```sakerscript
example.task(MyParam: {
	Value: str,
	Number: 123,
})
```

In the above example the build system will automatically implement the `MyParamType` interface and create an object that forwards its `getX()` calls to the underlying map. In this case calling `getValue()` on the assigned parameter object will return `"str"`, and calling `getNumber()` will return `123`.

The conversion also supports the `default` methods, therefore if the underlying map doesn't contain the associated entry, the default implementation of the method is called. In the above case calling `getFloating()` will return `1.0` as seen in the default implementation.

