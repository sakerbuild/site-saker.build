package saker.cmdref;

import java.io.EOFException;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import saker.build.file.ByteArraySakerFile;
import saker.build.file.SakerDirectory;
import saker.build.file.SakerFile;
import saker.build.file.provider.SakerPathFiles;
import saker.build.runtime.execution.ExecutionContext;
import saker.build.task.ParameterizableTask;
import saker.build.task.TaskContext;
import saker.build.task.utils.annot.SakerInput;
import saker.build.thirdparty.saker.util.ObjectUtils;
import saker.build.thirdparty.saker.util.StringUtils;
import saker.build.thirdparty.saker.util.io.ByteArrayRegion;
import saker.build.thirdparty.saker.util.io.DataInputUnsyncByteArrayInputStream;
import saker.build.thirdparty.saker.util.io.SerialUtils;
import saker.build.thirdparty.saker.util.io.UnsyncByteArrayOutputStream;
import saker.nest.utils.FrontendTaskFactory;
import saker.std.api.file.location.ExecutionFileLocation;
import saker.std.api.file.location.FileLocationVisitor;
import saker.std.api.file.location.LocalFileLocation;
import saker.std.main.file.option.FileLocationTaskOption;
import saker.std.main.file.utils.TaskOptionUtils;

public class CommandLineReferenceMarkdownTaskFactory extends FrontendTaskFactory<Object> {
	private static final long serialVersionUID = 1L;

	public static final String TASK_NAME = "saker.cmd.reference";

	private static final class TaskImpl implements ParameterizableTask<Object> {
		@SakerInput(value = { "", "ReferenceFilePath" }, required = true)
		public FileLocationTaskOption referenceFilePathOption;

		@SakerInput("Name")
		public String nameOption;

		@Override
		public Object run(TaskContext taskcontext) throws Exception {
			ByteArrayRegion[] filebytes = { null };
			TaskOptionUtils.toFileLocation(referenceFilePathOption, taskcontext).accept(new FileLocationVisitor() {
				@Override
				public void visit(ExecutionFileLocation loc) {
					SakerFile f = taskcontext.getTaskUtilities().resolveFileAtPath(loc.getPath());
					if (f == null) {
						throw ObjectUtils.sneakyThrow(new FileNotFoundException(
								"Reference file not found at path: " + referenceFilePathOption));
					}
					taskcontext.getTaskUtilities().reportInputFileDependency(null, f);
					try {
						filebytes[0] = f.getBytes();
					} catch (IOException e) {
						throw ObjectUtils.sneakyThrow(e);
					}
				}

				@Override
				public void visit(LocalFileLocation loc) {
					// TODO Auto-generated method stub
					FileLocationVisitor.super.visit(loc);
				}
			});

			String name = ObjectUtils.nullDefault(nameOption, "default");
			SakerDirectory outdir = SakerPathFiles.requireBuildDirectory(taskcontext).getDirectoryCreate(TASK_NAME)
					.getDirectoryCreate(name);

			outdir.clear();

			List<CommandModel> commands = new ArrayList<>();
			try (DataInputUnsyncByteArrayInputStream dis = new DataInputUnsyncByteArrayInputStream(filebytes[0])) {
				//version
				int version = dis.readInt();
				while (true) {
					List<String> cmdpath;
					try {
						cmdpath = SerialUtils.readExternalUTFCollection(new ArrayList<>(), dis);
					} catch (EOFException e) {
						break;
					}
					String usage = dis.readUTF();
					String doccomment = dis.readUTF();
					int paramcount = dis.readInt();

					CommandModel cmdmodel = new CommandModel(cmdpath, usage, doccomment);
					commands.add(cmdmodel);
					while (paramcount-- > 0) {
						List<String> names = SerialUtils.readExternalUTFCollection(new ArrayList<>(), dis);
						List<String> flags = SerialUtils.readExternalUTFCollection(new ArrayList<>(), dis);
						List<String> metanames = SerialUtils.readExternalUTFCollection(new ArrayList<>(), dis);
						String paramdoccomment = dis.readUTF();
						String paramformat = dis.readUTF();

						ParameterModel parammodel = new ParameterModel(names, flags, metanames, paramdoccomment,
								paramformat);
						cmdmodel.addParameter(parammodel);
					}

				}
			}
			for (CommandModel cmd : commands) {
				List<String> cmdpath = cmd.getCommandPath();
				String filename = getCommandPathFileName(cmdpath);

				String usage = cmd.getUsage();
				String doccomment = cmd.getDocComment();

				ByteArrayRegion bytes;
				try (UnsyncByteArrayOutputStream baos = new UnsyncByteArrayOutputStream()) {
					try (PrintStream ps = new PrintStream(baos)) {
						printSubSections(ps, cmdpath, commands);

						String headename = cmdpath.isEmpty() ? "Command line reference"
								: cmdpath.get(cmdpath.size() - 1);
						ps.println("# " + headename);
						ps.println();
						ps.println("<div class=\"doc-cmdref-cmd-usage\">");
						ps.println();
						ps.println(escapeHtml(usage));
						ps.println();
						ps.println("</div>");
						ps.println();

						ps.println("<div class=\"doc-cmdref-cmd-doc\">");
						ps.println();
						appendDocComment(ps, doccomment);
						ps.println();
						ps.println("</div>");

						if (hasSubSection(cmdpath, commands)) {
							ps.println();
							ps.println("##### Sub-commands");
							ps.println();
							ps.println("[!tableofcontents]()");
						}

						for (ParameterModel pm : cmd.getParameters()) {
							List<String> names = pm.getNames();
							List<String> flags = pm.getFlags();
							List<String> metanames = pm.getMetaNames();
							String paramdoccomment = pm.getDocComment();
							String paramformat = pm.getFormat();

							boolean positional = flags.contains("positional");
							boolean mapparameter = flags.contains("map-parameter");

							ps.println();
							ps.println("##### " + names.iterator().next());
							ps.println();

							if (!positional) {
								ps.print("<div class=\"doc-cmdref-param-aliases\">");
								for (String n : names) {
									ps.print(n);
									if (mapparameter) {
										ps.print("&lt;key&gt;=&lt;value&gt;");
									}
									if (!paramformat.isEmpty()) {
										ps.print(' ');
										ps.print(escapeHtml(paramformat));
									}
									ps.println();
								}
								ps.println("</div>");
							} else {
								if (!paramformat.isEmpty()) {
									ps.print("<div class=\"doc-cmdref-param-format\">");
									ps.print(escapeHtml(paramformat));
									ps.println("</div>");
								}
							}
							ps.println();
							if (!metanames.isEmpty()) {
								ps.println("<div class=\"doc-cmdref-param-meta\">");
								for (String mn : metanames) {
									ps.print("<span class=\"dod-cmdref-param-meta-name\">" + mn + "</span>");
								}
								ps.println("</div>");
								ps.println();
							}
							ps.println("<div class=\"doc-cmdref-param-flags\">");
							if (flags.contains("required")) {
								ps.println("<b>Required parameter.</b> ");
							}
							if (positional) {
								ps.println("Positional parameter. ");
							}
							if (flags.contains("deprecated")) {
								ps.println("Deprecated. ");
							}
							if (flags.contains("multi-parameter")) {
								ps.println("Multi-parameter. ");
							}
							ps.println("</div>");
							ps.println();

							ps.println("<div class=\"doc-cmdref-param-doc\">");
							ps.println();
							appendDocComment(ps, paramdoccomment);
							ps.println();
							ps.println("</div>");

							ps.println();
						}
					}
					bytes = baos.toByteArrayRegion();
				}
				ByteArraySakerFile outfile = new ByteArraySakerFile(filename, bytes);
				outdir.add(outfile);
				taskcontext.getTaskUtilities().reportOutputFileDependency(null, outfile);

			}
			outdir.synchronize();
			return outdir.getSakerPath();
		}

	}

	@Override
	public ParameterizableTask<? extends Object> createTask(ExecutionContext executioncontext) {
		return new TaskImpl();
	}

	private static String escapeHtml(String usage) {
		return usage.replace("<", "&lt;").replace(">", "&gt;");
	}

	private static String getCommandPathFileName(List<String> cmdpath) {
		String filename;
		if (cmdpath.isEmpty()) {
			filename = "index.md";
		} else {
			filename = StringUtils.toStringJoin(null, "_", cmdpath, ".md");
		}
		return filename;
	}

	private static void appendDocComment(PrintStream ps, String doccomment) {
		appendLinesBlock(ps, splitToLines(escapeHtml(doccomment)));
	}

	private static List<String> splitToLines(String s) {
		//removes leading and trailing empty lines

		String[] split = s.split("\n");
		ArrayList<String> result = new ArrayList<>();
		int i = 0;
		for (; i < split.length; ++i) {
			String spl = split[i];
			if (spl.isEmpty()) {
				continue;
			}
			if (getLeadingSpaceCount(spl) == spl.length()) {
				continue;
			}
			break;
		}
		for (; i < split.length; ++i) {
			String spl = split[i];
			result.add(spl);
		}
		int size = result.size();
		for (int j = size - 1; j >= 0; j--) {
			String spl = result.get(j);
			if (spl.isEmpty() || getLeadingSpaceCount(spl) == spl.length()) {
				result.remove(j);
				continue;
			}
			break;
		}
		return result;
	}

	private static int getLeadingSpaceCount(CharSequence cs) {
		int len = cs.length();
		for (int i = 0; i < len; i++) {
			if (cs.charAt(i) != ' ') {
				return i;
			}
		}
		return len;
	}

	private static void appendLinesBlock(PrintStream ps, Iterable<String> lines) {
		int spacecount = getAllLeadingSpaceCount(lines);
		for (String l : lines) {
			l = removeLeadingSpaceCount(l, spacecount);
			ps.println(l);
		}
	}

	private static String removeLeadingSpaceCount(String s, int spacecount) {
		if (spacecount > s.length()) {
			return "";
		}
		return s.substring(spacecount);
	}

	private static int getAllLeadingSpaceCount(Iterable<String> lines) {
		int c = Integer.MAX_VALUE;
		Iterator<String> it = lines.iterator();
		if (!it.hasNext()) {
			return 0;
		}
		while (it.hasNext()) {
			String l = it.next();
			int lsc = getLeadingSpaceCount(l);
			if (lsc == 0) {
				return 0;
			}
			if (lsc != l.length()) {
				c = Math.min(c, lsc);
			}
		}
		if (c == Integer.MAX_VALUE) {
			return 0;
		}
		return c;
	}

	private static void printSubSections(PrintStream ps, List<String> cmdpath, Collection<CommandModel> commands) {
		boolean had = false;
		for (CommandModel cm : commands) {
			List<String> currentcmdpath = cm.getCommandPath();
			if (currentcmdpath.size() != cmdpath.size() + 1) {
				continue;
			}
			if (!currentcmdpath.subList(0, cmdpath.size()).equals(cmdpath)) {
				continue;
			}
			ps.println("[!section](" + getCommandPathFileName(currentcmdpath) + ")");
			had = true;
		}
		if (had) {
			ps.println();
		}
	}

	private static boolean hasSubSection(List<String> cmdpath, Collection<CommandModel> commands) {
		for (CommandModel cm : commands) {
			List<String> currentcmdpath = cm.getCommandPath();
			if (currentcmdpath.size() != cmdpath.size() + 1) {
				continue;
			}
			if (!currentcmdpath.subList(0, cmdpath.size()).equals(cmdpath)) {
				continue;
			}
			return true;
		}
		return false;
	}

	private static class CommandModel {
		private String usage;
		private String docComment;
		private List<String> commandPath;
		private List<ParameterModel> parameters = new ArrayList<>();

		public CommandModel(List<String> cmdpath, String usage, String doccomment) {
			this.commandPath = cmdpath;
			this.usage = usage;
			this.docComment = doccomment;
		}

		public String getUsage() {
			return usage;
		}

		public List<String> getCommandPath() {
			return commandPath;
		}

		public String getDocComment() {
			return docComment;
		}

		public List<ParameterModel> getParameters() {
			return parameters;
		}

		public void addParameter(ParameterModel param) {
			this.parameters.add(param);
		}
	}

	private static class ParameterModel {
		private List<String> names;
		private List<String> flags;
		private List<String> metaNames;
		private String docComment;
		private String format;

		public ParameterModel(List<String> names, List<String> flags, List<String> metanames, String paramdoccomment,
				String paramformat) {
			this.names = names;
			this.flags = flags;
			this.metaNames = metanames;
			this.docComment = paramdoccomment;
			this.format = paramformat;
		}

		public List<String> getNames() {
			return names;
		}

		public List<String> getFlags() {
			return flags;
		}

		public String getDocComment() {
			return docComment;
		}

		public String getFormat() {
			return format;
		}

		public List<String> getMetaNames() {
			return metaNames;
		}
	}
}
