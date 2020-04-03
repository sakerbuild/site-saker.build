package example;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.NavigableMap;
import java.util.Objects;

import saker.build.runtime.execution.ExecutionContext;
import saker.build.task.ParameterizableTask;
import saker.build.task.Task;
import saker.build.task.TaskContext;
import saker.build.task.TaskFactory;
import saker.build.task.exception.TaskParameterException;
import saker.build.task.identifier.TaskIdentifier;
import saker.build.thirdparty.saker.util.ObjectUtils;

public class ExampleParameterizedTaskFactory 
		implements TaskFactory<Void>, Externalizable {
	public ExampleParameterizedTaskFactory() {
	}

	@Override
	public Task<? extends Void> createTask(ExecutionContext executioncontext) {
		return new ParameterizableTask<Void>() {
			private Object parameter;

			@Override
			public Void run(TaskContext taskcontext) throws Exception {
				taskcontext.println(Objects.toString(parameter));
				return null;
			}

			@Override
			public void initParameters(TaskContext taskcontext,
					NavigableMap<String, ? extends TaskIdentifier> parameters)
							throws TaskParameterException {
				parameter = taskcontext.getTaskResult(parameters.get("Parameter"));
			}
		};
	}
	//snippet-end

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
	}

	@Override
	public void readExternal(ObjectInput in) 
			throws IOException, ClassNotFoundException {
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