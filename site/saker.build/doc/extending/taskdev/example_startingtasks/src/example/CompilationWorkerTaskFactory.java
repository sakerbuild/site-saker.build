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

public class CompilationWorkerTaskFactory 
		implements TaskFactory<Void>, Externalizable {
	private SakerPath sourcePath;

	/**
	* For {@link Externalizable}.
	*/
	public CompilationWorkerTaskFactory() {
	}
	
	public CompilationWorkerTaskFactory(SakerPath sourcePath) {
		this.sourcePath = sourcePath;
	}


	@Override
	public Task<? extends Void> createTask(ExecutionContext executioncontext) {
		return new ParameterizableTask<Void>() {
			@Override
			//snippet-start
			public Void run(TaskContext taskcontext) throws Exception {
				SakerFile sourcefile = taskcontext.getTaskUtilities()
						.resolveFileAtPath(CompilationWorkerTaskFactory.this.sourcePath);
				if (sourcefile == null) {
					throw new NoSuchFileException(sourcePath.toString(), null, 
							"Source file not found.");
				}
				taskcontext.getTaskUtilities()
					.reportInputFileDependency(null, sourcefile);
				// actually compile the source file
				return null;
			}
			//snippet-end
		};
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeObject(sourcePath);
	}

	@Override
	public void readExternal(ObjectInput in) 
				throws IOException, ClassNotFoundException {
		sourcePath = (SakerPath) in.readObject();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((sourcePath == null) ? 0 : sourcePath.hashCode());
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
		CompilationWorkerTaskFactory other = (CompilationWorkerTaskFactory) obj;
		if (sourcePath == null) {
			if (other.sourcePath != null)
				return false;
		} else if (!sourcePath.equals(other.sourcePath))
			return false;
		return true;
	}
	
}