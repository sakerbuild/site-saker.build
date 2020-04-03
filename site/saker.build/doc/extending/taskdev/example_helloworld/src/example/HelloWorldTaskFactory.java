package example;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import saker.build.runtime.execution.ExecutionContext;
import saker.build.task.Task;
import saker.build.task.TaskContext;
import saker.build.task.TaskFactory;
import saker.build.thirdparty.saker.util.ObjectUtils;

public class HelloWorldTaskFactory 
		implements TaskFactory<Void>, Externalizable {
	public HelloWorldTaskFactory() {
	}

	@Override
	public Task<? extends Void> createTask(ExecutionContext executioncontext) {
		return new Task<Void>() {
			@Override
			public Void run(TaskContext taskcontext) throws Exception {
				taskcontext.println("Hello world!");
				return null;
			}
		};
	}

	@Override
	public void writeExternal(ObjectOutput out) 
			throws IOException {
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