package example;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import saker.build.runtime.execution.ExecutionContext;
import saker.build.task.Task;
import saker.build.task.TaskContext;
import saker.build.task.TaskFactory;

public class WorkerTaskFactory 
		implements TaskFactory<Object>, Externalizable {
	private int input;
	
	/**
	* For {@link Externalizable}.
	*/
	public WorkerTaskFactory() {
	}
	
	public WorkerTaskFactory(int input) {
		this.input = input;
	}
	
	@Override
	public Task<? extends Object> createTask(ExecutionContext executioncontext) {
		//snippet-start
		return new Task<Object>() {
			@Override
			public Object run(TaskContext taskcontext) throws Exception {
				// execute the work of the task based on the configuration ...
				//   return some result value of the task
				return WorkerTaskFactory.this.input;
			}
		};
		//snippet-end
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
	}

	@Override
	public void readExternal(ObjectInput in) 
			throws IOException, ClassNotFoundException {
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + input;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		WorkerTaskFactory other = (WorkerTaskFactory) obj;
		if (input != other.input)
			return false;
		return true;
	}
}