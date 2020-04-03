package example;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import saker.build.runtime.execution.ExecutionContext;
import saker.build.task.ParameterizableTask;
import saker.build.task.Task;
import saker.build.task.TaskContext;
import saker.build.task.TaskFactory;
import saker.build.task.utils.annot.SakerInput;
import saker.build.thirdparty.saker.util.ObjectUtils;

public class SummarizeTaskFactory 
		implements TaskFactory<Integer>, Externalizable {
	public SummarizeTaskFactory() {
	}

	@Override
	public Task<? extends Integer> createTask(ExecutionContext executioncontext) {
		return new ParameterizableTask<Integer>() {
			//snippet-start
			@SakerInput
			public int Left;
			@SakerInput
			public int Right;
			
			@Override
			public Integer run(TaskContext taskcontext) throws Exception {
				return this.Left + this.Right;
			}
			//snippet-end
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