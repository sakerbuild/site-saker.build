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
import saker.build.thirdparty.saker.util.ObjectUtils;

public class SumOutputFileTaskFactory 
		implements TaskFactory<Void>, Externalizable {
	public SumOutputFileTaskFactory() {
	}

	@Override
	public Task<? extends Void> createTask(ExecutionContext executioncontext) {
		return new ParameterizableTask<Void>() {
			//snippet-start
			@SakerInput
			public int Left;
			@SakerInput
			public int Right;

			@Override
			public Void run(TaskContext taskcontext) throws Exception {
				SakerDirectory builddir = taskcontext.getTaskBuildDirectory();
				if (builddir == null) {
					throw new MissingConfigurationException(
							"Task requires build directory for output.");
				}
				int sum = Left + Right;
				ByteArraySakerFile outfile = new ByteArraySakerFile("sum.txt", 
						Integer.toString(sum).getBytes(StandardCharsets.UTF_8));
				builddir.getDirectoryCreate("example.sum.file").add(outfile);
				outfile.synchronize();
				
				taskcontext.reportOutputFileDependency(null, 
						outfile.getSakerPath(), outfile.getContentDescriptor());
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