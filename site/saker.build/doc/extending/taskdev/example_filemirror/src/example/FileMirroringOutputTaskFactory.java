package example;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.nio.file.Path;

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

public class FileMirroringOutputTaskFactory 
		implements TaskFactory<Void>, Externalizable {
	public FileMirroringOutputTaskFactory() {
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
				ExecutionContext executioncontext = taskcontext.getExecutionContext();
				Path inputmirrorpath = taskcontext.mirror(inputfile);
				// we place the transformed file to the build directory
				SakerDirectory builddir = taskcontext.getTaskBuildDirectory();
				// the output path of the process execution is a mirror location
				Path outputpath = executioncontext.toMirrorPath(builddir.getSakerPath())
						.resolve("outfile.bin");
				
				// execute the external process that transforms the given file ...
				//   pass mirrorpath directly as the input argument
				//   set the output location with the -o flag for our process
				int rescode = new ProcessBuilder("transformfile.exe", 
						inputmirrorpath.toString(), "-o", outputpath.toString())
						.start().waitFor();
				if (rescode != 0) {
					throw new Exception("Failed. (" + rescode + ")");
				}
				// create the SakerFile backed by the result file
				SakerFile outfile = taskcontext.getTaskUtilities().createProviderPathFile(
						"outfile.bin", LocalFileProvider.getInstance().getPathKey(outputpath));
				// add it to the build directory
				builddir.add(outfile);
				// synchronize the output file
				outfile.synchronize();
				
				// report the files as a dependency
				taskcontext.getTaskUtilities().reportOutputFileDependency(null, outfile);
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