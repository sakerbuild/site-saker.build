package example;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.nio.file.Path;

import saker.build.file.SakerFile;
import saker.build.file.path.SakerPath;
import saker.build.runtime.execution.ExecutionContext;
import saker.build.task.ParameterizableTask;
import saker.build.task.Task;
import saker.build.task.TaskContext;
import saker.build.task.TaskFactory;
import saker.build.task.utils.annot.SakerInput;
import saker.build.thirdparty.saker.util.ObjectUtils;

public class FileMirroringTaskFactory 
		implements TaskFactory<Void>, Externalizable {
	public FileMirroringTaskFactory() {
	}

	@Override
	public Task<? extends Void> createTask(ExecutionContext executioncontext) {
		return new ParameterizableTask<Void>() {
			//snippet-start
			@SakerInput(required = true)
			public SakerPath File;
			
			@Override
			public Void run(TaskContext taskcontext) throws Exception {
				SakerFile inputfile = taskcontext.getTaskUtilities()
						.resolveAtPath(this.File);
				Path mirrorpath = taskcontext.mirror(inputfile);
				// execute the external process that consumes the given file ...
				//   pass mirrorpath directly as the input argument
				int rescode = new ProcessBuilder("myprocess.exe", mirrorpath.toString())
						.start().waitFor();
				if (rescode != 0) {
					throw new Exception("Failed. (" + rescode + ")");
				}
				// report the file as a task input
				taskcontext.getTaskUtilities().reportInputFileDependency(null, inputfile);
				return null;
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