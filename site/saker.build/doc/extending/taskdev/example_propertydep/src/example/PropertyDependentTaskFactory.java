package example;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.nio.charset.StandardCharsets;
import java.nio.file.NoSuchFileException;

import saker.build.exception.MissingConfigurationException;
import saker.build.file.ByteArraySakerFile;
import saker.build.file.SakerDirectory;
import saker.build.file.SakerFile;
import saker.build.file.path.SakerPath;
import saker.build.runtime.execution.ExecutionContext;
import saker.build.task.ParameterizableTask;
import saker.build.task.Task;
import saker.build.task.TaskContext;
import saker.build.task.TaskFactory;
import saker.build.task.utils.annot.SakerInput;
import saker.build.util.property.UserParameterEnvironmentProperty;
import saker.build.util.property.UserParameterExecutionProperty;
import saker.build.thirdparty.saker.util.ObjectUtils;

public class PropertyDependentTaskFactory 
		implements TaskFactory<String>, Externalizable {
	public PropertyDependentTaskFactory() {
	}

	@Override
	public Task<? extends String> createTask(ExecutionContext executioncontext) {
		return new ParameterizableTask<String>() {
			@Override
			//snippet-start
			public String run(TaskContext taskcontext) throws Exception {
				String exuserparam = taskcontext.getTaskUtilities()
						.getReportExecutionDependency(
								new UserParameterExecutionProperty("execution.user.parameter"));
				String envuserparam = taskcontext.getTaskUtilities()
						.getReportEnvironmentDependency(
								new UserParameterEnvironmentProperty("environment.user.parameter"));
				//do something more meaningful with the values
				return exuserparam + envuserparam;
			}
			//snippet-end
		};
	}

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