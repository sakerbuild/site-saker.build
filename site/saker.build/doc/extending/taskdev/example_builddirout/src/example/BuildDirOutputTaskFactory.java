package example;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.nio.file.Path;

import saker.build.file.ByteArraySakerFile;
import saker.build.file.SakerDirectory;
import saker.build.file.SakerFile;
import saker.build.file.path.SakerPath;
import saker.build.file.provider.LocalFileProvider;
import saker.build.runtime.execution.ExecutionContext;
import saker.build.task.ParameterizableTask;
import saker.build.task.Task;
import saker.build.task.TaskContext;
import saker.build.task.TaskFactory;
import saker.build.task.utils.annot.SakerInput;
import saker.build.thirdparty.saker.util.ObjectUtils;

public class BuildDirOutputTaskFactory 
		implements TaskFactory<Void>, Externalizable {
	public BuildDirOutputTaskFactory() {
	}

	@Override
	public Task<? extends Void> createTask(ExecutionContext executioncontext) {
		return new ParameterizableTask<Void>() {
			@Override
			public Void run(TaskContext taskcontext) throws Exception {
				//snippet-start
				SakerFile outputfile = new ByteArraySakerFile("output.txt", 
						"outdata".getBytes());
				// determine the output directory
				SakerDirectory outputdir = taskcontext.getTaskUtilities()
						.resolveDirectoryAtPathCreate(taskcontext.getTaskBuildDirectory(), 
								SakerPath.valueOf("example.task/default"));
				outputdir.add(outputfile);
				// synchronize the output file
				outputfile.synchronize();
				
				// report the files as a dependency
				taskcontext.getTaskUtilities().reportOutputFileDependency(null, outputfile);
				//snippet-end
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