package example;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.NavigableMap;

import saker.build.file.SakerFile;
import saker.build.file.path.SakerPath;
import saker.build.file.path.WildcardPath;
import saker.build.runtime.execution.ExecutionContext;
import saker.build.task.CommonTaskContentDescriptors;
import saker.build.task.ParameterizableTask;
import saker.build.task.Task;
import saker.build.task.TaskContext;
import saker.build.task.TaskFactory;
import saker.build.task.utils.annot.SakerInput;
import saker.build.task.utils.dependencies.WildcardFileCollectionStrategy;
import saker.build.thirdparty.saker.util.ObjectUtils;

public class CompileBootstrapperTaskFactory 
		implements TaskFactory<Void>, Externalizable {
	public CompileBootstrapperTaskFactory() {
	}

	@Override
	public Task<? extends Void> createTask(ExecutionContext executioncontext) {
		return new ParameterizableTask<Void>() {
			//snippet-start
			@SakerInput(required = true)
			public WildcardPath Sources;
			
			@Override
			public Void run(TaskContext taskcontext) throws Exception {
				NavigableMap<SakerPath, SakerFile> files = taskcontext.getTaskUtilities()
						.collectFilesReportAdditionDependency(null,
								WildcardFileCollectionStrategy.create(Sources));
				for (SakerPath path : files.keySet()) {
					taskcontext.reportInputFileDependency(null, 
							path, CommonTaskContentDescriptors.PRESENT);
					taskcontext.startTask(
							new WorkerTaskIdentifier(path), 
							new CompilationWorkerTaskFactory(path), 
							null);
				}
				return null;
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