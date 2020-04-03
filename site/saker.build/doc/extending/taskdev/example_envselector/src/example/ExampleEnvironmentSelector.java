package example;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Collections;

import saker.build.runtime.environment.SakerEnvironment;
import saker.build.task.EnvironmentSelectionResult;
import saker.build.task.TaskExecutionEnvironmentSelector;
import saker.build.util.property.UserParameterEnvironmentProperty;
import saker.build.thirdparty.saker.util.ObjectUtils;

public class ExampleEnvironmentSelector implements TaskExecutionEnvironmentSelector, Externalizable {
	public ExampleEnvironmentSelector() {
	}
	
	//snippet-start
	@Override
	public EnvironmentSelectionResult isSuitableExecutionEnvironment(
			SakerEnvironment environment) {
		UserParameterEnvironmentProperty property = 
				new UserParameterEnvironmentProperty(
						"example.required.tool.version");
		String toolversion = environment
				.getEnvironmentPropertyCurrentValue(property);
		if (toolversion == null) {
			return null;
		}
		return new EnvironmentSelectionResult(
				Collections.singletonMap(property, toolversion));
	}
	//snippet-end

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
	}

	@Override
	public int hashCode() {
		return getClass().getName().hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		return ObjectUtils.isSameClass(this, obj);
	}
}
