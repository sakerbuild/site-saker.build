package example;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.NavigableMap;
import java.util.TreeMap;

import saker.build.file.ByteArraySakerFile;
import saker.build.file.SakerDirectory;
import saker.build.file.SakerFile;
import saker.build.file.path.SakerPath;
import saker.build.file.provider.LocalFileProvider;
import saker.build.ide.configuration.SimpleIDEConfiguration;
import saker.build.runtime.execution.ExecutionContext;
import saker.build.task.ParameterizableTask;
import saker.build.task.Task;
import saker.build.task.TaskContext;
import saker.build.task.TaskFactory;
import saker.build.task.utils.annot.SakerInput;
import saker.build.thirdparty.saker.util.ObjectUtils;

public class IdeConfigTaskFactory 
		implements TaskFactory<Void>, Externalizable {
	public IdeConfigTaskFactory() {
	}

	@Override
	public Task<? extends Void> createTask(ExecutionContext executioncontext) {
		return new Task<Void>() {
			@Override
			public Void run(TaskContext taskcontext) throws Exception {
				//snippet-start
				NavigableMap<String, Object> fields = new TreeMap<>();
				fields.put("source_directories", Arrays.asList("src/main", "api/main"));
				fields.put("classpath", "wd:/my_lib.jar");
				fields.put("source_version", 8);
				
				taskcontext.reportIDEConfiguration(
						new SimpleIDEConfiguration("java.compile.ideconfig", "main", fields));
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