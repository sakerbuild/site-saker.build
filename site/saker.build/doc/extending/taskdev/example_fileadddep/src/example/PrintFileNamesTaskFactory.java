package example;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.nio.file.NoSuchFileException;
import java.util.NavigableMap;

import saker.build.file.SakerFile;
import saker.build.file.path.SakerPath;
import saker.build.file.path.WildcardPath;
import saker.build.runtime.execution.ExecutionContext;
import saker.build.task.ParameterizableTask;
import saker.build.task.Task;
import saker.build.task.TaskContext;
import saker.build.task.TaskFactory;
import saker.build.task.dependencies.FileCollectionStrategy;
import saker.build.task.utils.annot.SakerInput;
import saker.build.task.utils.dependencies.WildcardFileCollectionStrategy;
import saker.build.thirdparty.saker.util.ObjectUtils;

public class PrintFileNamesTaskFactory 
		implements TaskFactory<Void>, Externalizable {
	public PrintFileNamesTaskFactory() {
	}

	@Override
	public Task<? extends Void> createTask(ExecutionContext executioncontext) {
		return new ParameterizableTask<Void>() {
			//snippet-start
			@SakerInput(required = true)
			public WildcardPath Files;

			@Override
			public Void run(TaskContext taskcontext) throws Exception {
				FileCollectionStrategy additiondep = WildcardFileCollectionStrategy.create(Files);
				NavigableMap<SakerPath, SakerFile> foundfiles = 
						taskcontext.getTaskUtilities()
							.collectFilesReportInputFileAndAdditionDependency(null, additiondep);
				for (SakerPath filepath : foundfiles.keySet()) {
					taskcontext.println(filepath.toString());
				}
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