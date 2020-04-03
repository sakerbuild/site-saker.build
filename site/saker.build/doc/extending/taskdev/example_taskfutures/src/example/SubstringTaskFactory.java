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
import saker.build.task.TaskFuture;
import saker.build.task.exception.TaskParameterException;
import saker.build.task.identifier.TaskIdentifier;
import saker.build.task.utils.annot.SakerInput;
import saker.build.thirdparty.saker.util.ObjectUtils;

public class SubstringTaskFactory 
		implements TaskFactory<String>, Externalizable {
	public SubstringTaskFactory() {
	}

	@Override
	public Task<? extends String> createTask(ExecutionContext executioncontext) {
		return new ParameterizableTask<String>() {
			//snippet-start
			public TaskIdentifier inputTaskId;
			@SakerInput
			public int Start;
			@SakerInput
			public Integer End;

			@Override
			public String run(TaskContext taskcontext) throws Exception {
				TaskFuture<?> inputfuture = taskcontext.getTaskFuture(inputTaskId);
				String input = Objects.toString(inputfuture.get());
				int end = this.End == null ? input.length() : this.End;
				return input.substring(Start, end);
			}

			@Override
			public void initParameters(TaskContext taskcontext, 
					NavigableMap<String, ? extends TaskIdentifier> parameters) 
							throws TaskParameterException {
				this.inputTaskId = parameters.get("");
				ParameterizableTask.super.initParameters(taskcontext, parameters);
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