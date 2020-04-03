package example;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.nio.file.NoSuchFileException;

import saker.build.file.SakerFile;
import saker.build.file.path.SakerPath;
import saker.build.runtime.execution.ExecutionContext;
import saker.build.task.ParameterizableTask;
import saker.build.task.Task;
import saker.build.task.TaskContext;
import saker.build.task.TaskFactory;
import saker.build.task.utils.annot.SakerInput;
import saker.build.thirdparty.saker.util.ObjectUtils;

public class FileContentsTaskFactory 
		implements TaskFactory<String>, Externalizable {
	public FileContentsTaskFactory() {
	}

	@Override
	public Task<? extends String> createTask(ExecutionContext executioncontext) {
		return new ParameterizableTask<String>() {
			//snippet-start
			@SakerInput(required = true)
			public SakerPath Path;

			@Override
			public String run(TaskContext taskcontext) throws Exception {
				SakerFile file = taskcontext.getTaskUtilities().resolveAtPath(Path);
				if (file == null) {
					throw new NoSuchFileException(Path.toString());
				}
				taskcontext.reportInputFileDependency(null, 
						Path, file.getContentDescriptor());
				return file.getContent();
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