package saker.preprocess;

import java.io.FileNotFoundException;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;

import saker.build.file.SakerFile;
import saker.build.file.path.SakerPath;
import saker.build.runtime.execution.ExecutionContext;
import saker.build.task.ParameterizableTask;
import saker.build.task.TaskContext;
import saker.build.task.utils.annot.SakerInput;
import saker.build.thirdparty.saker.util.ImmutableUtils;
import saker.nest.utils.FrontendTaskFactory;

public class PreprocessorTaskFactory extends FrontendTaskFactory<String> {
	private static final long serialVersionUID = 1L;

	private static final class TaskImpl implements ParameterizableTask<String> {
		@SakerInput(value = { "", "Input" }, required = true)
		public SakerPath inputOption;

		@SakerInput("Macros")
		public Map<String, String> macrosOption = Collections.emptyNavigableMap();

		@SakerInput("EmbedMacros")
		public Map<String, SakerPath> embedMacrosOption = Collections.emptyNavigableMap();

		@Override
		public String run(TaskContext taskcontext) throws Exception {
			long nanos = System.nanoTime();
			SakerFile templatefile = taskcontext.getTaskUtilities().resolveAtPath(inputOption);
			if (templatefile == null) {
				throw new FileNotFoundException("Template file not found: " + inputOption);
			}
			taskcontext.getTaskUtilities().reportInputFileDependency(null, templatefile);

			Map<String, SakerPath> embedmacros = ImmutableUtils.makeImmutableLinkedHashMap(embedMacrosOption);
			Map<String, String> macros = ImmutableUtils.makeImmutableLinkedHashMap(macrosOption);

			String templatefilecontents = templatefile.getContent();
			Set<String> resolvedmacros = new TreeSet<>();
			for (Iterator<Entry<String, SakerPath>> it = embedmacros.entrySet().iterator(); it.hasNext();) {
				Entry<String, SakerPath> entry = it.next();
				String macro = entry.getKey();
				if (!templatefilecontents.contains(macro)) {
					continue;
				}
				if (!resolvedmacros.add(macro)) {
					throw new IllegalArgumentException("Recursive embed macro: " + macro);
				}
				SakerFile embedfile = taskcontext.getTaskUtilities().resolveFileAtPath(entry.getValue());
				if (embedfile == null) {
					throw new FileNotFoundException("Embed file not found: " + entry.getValue());
				}
				taskcontext.getTaskUtilities().reportInputFileDependency(null, embedfile);
				templatefilecontents = templatefilecontents.replace(macro, embedfile.getContent());
				it = embedmacros.entrySet().iterator();
			}
			for (Entry<String, String> entry : macros.entrySet()) {
				templatefilecontents = templatefilecontents.replace(entry.getKey(), entry.getValue());
			}

			System.out.println(
					"PreprocessorTaskFactory.TaskImpl.run() " + (System.nanoTime() - nanos) / 1_000_000 + " ms");

			return templatefilecontents;
		}
	}

	@Override
	public ParameterizableTask<? extends String> createTask(ExecutionContext executioncontext) {
		return new TaskImpl();
	}

}
