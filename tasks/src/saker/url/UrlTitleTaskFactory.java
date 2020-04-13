package saker.url;

import java.io.Externalizable;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

import saker.build.file.SakerFile;
import saker.build.file.path.SakerPath;
import saker.build.runtime.execution.ExecutionContext;
import saker.build.runtime.execution.FileDataComputer;
import saker.build.runtime.execution.SakerLog;
import saker.build.task.Task;
import saker.build.task.TaskContext;
import saker.build.task.TaskFactory;
import saker.build.task.identifier.TaskIdentifier;
import saker.build.thirdparty.saker.util.ObjectUtils;
import saker.build.thirdparty.saker.util.io.ByteArrayRegion;
import saker.build.thirdparty.saker.util.io.StreamUtils;

public class UrlTitleTaskFactory implements TaskFactory<String>, Task<String>, Externalizable, TaskIdentifier {
	private static final long serialVersionUID = 1L;

	private String url;

	/**
	 * For {@link Externalizable}.
	 */
	public UrlTitleTaskFactory() {
	}

	public UrlTitleTaskFactory(String url) {
		this.url = url;
	}

	@Override
	public String run(TaskContext taskcontext) throws Exception {
		String prevout = taskcontext.getPreviousTaskOutput(String.class);
		if (prevout != null) {
			return prevout;
		}
		taskcontext.setStandardOutDisplayIdentifier("url.title");

		SakerFile titlestile = taskcontext.getTaskUtilities().resolveFileAtPath(SakerPath.valueOf("url_titles.txt"));
		if (titlestile != null) {
			taskcontext.getTaskUtilities().reportInputFileDependency(null, titlestile);
			Map<String, String> urltitles = taskcontext.computeFileContentData(titlestile, new TitleFileDataComputer());
			String foundtitle = urltitles.get(url);
			if (foundtitle != null) {
				return foundtitle;
			}
		}

		long nanos = System.nanoTime();
		SakerLog.log().verbose().println("Retrieving title for: " + url);
		URL url = new URL(this.url);
		if ("api.nest.saker.build".equals(url.getHost())) {
			SakerPath path = SakerPath.valueOf(url.getPath());
			switch (path.getNameCount()) {
				case 3: {
					switch (path.getName(0)) {
						case "bundle": {
							switch (path.getName(1)) {
								case "download": {
									return "Download " + path.getName(2);
								}
								default: {
									break;
								}
							}
							break;
						}
						default: {
							break;
						}
					}
					break;
				}
			}
			throw new IllegalArgumentException("Unrecognized path for nest URL: " + url + " with " + path);
		}
		if ("nest.saker.build".equals(url.getHost())) {
			SakerPath path = SakerPath.valueOf(url.getPath());
			switch (path.getNameCount()) {
				case 0: {
					return "Saker.nest plugin repository";
				}
				case 1: {
					switch (path.getName(0)) {
						case "terms": {
							return "Terms of service | saker.nest";
						}
						case "privacy": {
							return "Privacy policy | saker.nest";
						}
						default: {
							break;
						}
					}
					break;
				}
				case 2: {
					switch (path.getName(0)) {
						case "package": {
							return path.getName(1) + " | saker.nest";
						}
						case "user": {
							switch (path.getName(1)) {
								case "packages": {
									return "Manage packages | saker.nest";
								}
								default: {
									break;
								}
							}
							break;
						}
						default: {
							break;
						}
					}
					break;
				}
				case 3: {
					switch (path.getName(0)) {
						case "bundle": {
							switch (path.getName(1)) {
								case "download": {
									return "Download " + path.getName(2);
								}
								default: {
									break;
								}
							}
							break;
						}
						default: {
							break;
						}
					}
					break;
				}
			}
			throw new IllegalArgumentException("Unrecognized path for nest URL: " + url + " with " + path);
		}
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		connection.addRequestProperty("User-Agent",
				"Mozilla/5.0 (Windows NT 6.1; WOW64; rv:25.0) Gecko/20100101 Firefox/25.0");
		connection.addRequestProperty("Accept-Language", "en-US;q=0.8,en;q=0.7");
		connection.addRequestProperty("Cache-control", "no-cache");
		connection.addRequestProperty("Pragma", "no-cache");
		connection.setConnectTimeout(10000);
		connection.setReadTimeout(10000);
		connection.setUseCaches(false);
		String str;
		try (InputStream in = connection.getInputStream()) {
			str = StreamUtils.readStreamStringFully(in);
		} catch (IOException e) {
			throw new RuntimeException("Failed to query URL title: " + url, e);
		}
		long time = (System.nanoTime() - nanos) / 1_000_000;
		if (time > 100) {
			System.out.println("Retrieving " + this.url + " took " + time + " ms");
		}
		String lcstr = str.toLowerCase(Locale.ENGLISH);
		int titleidx = lcstr.indexOf("<title>");
		if (titleidx < 0) {
			SakerLog.warning().println("No title found for: " + url);
			return null;
		}
		int endidx = lcstr.indexOf("</title>");
		if (endidx < 0) {
			SakerLog.warning().println("No <title> closing tag found for: " + url);
			return null;
		}
		StringBuilder sb = new StringBuilder();
		str.substring(titleidx + 7, endidx).trim().codePoints().forEach(cp -> {
			if (cp >= 32 && cp <= 126) {
				sb.appendCodePoint(cp);
			} else {
				sb.append("&#");
				sb.append(cp);
				sb.append(";");
			}
		});
		String result = sb.toString();
		taskcontext.acquireStandardIOLock();
		try {
			taskcontext.getStandardOut().write(ByteArrayRegion.wrap((this.url + "\n").getBytes()));
			taskcontext.getStandardOut().write(ByteArrayRegion.wrap(("\t" + result + "\n").getBytes()));
		} finally {
			taskcontext.releaseStandardIOLock();
		}
		return result;
	}

	@Override
	public Task<? extends String> createTask(ExecutionContext executioncontext) {
		return this;
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeObject(url);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		url = (String) in.readObject();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((url == null) ? 0 : url.hashCode());
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
		UrlTitleTaskFactory other = (UrlTitleTaskFactory) obj;
		if (url == null) {
			if (other.url != null)
				return false;
		} else if (!url.equals(other.url))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "UrlTitleTaskFactory[" + (url != null ? "url=" + url : "") + "]";
	}

	private static final class TitleFileDataComputer implements FileDataComputer<Map<String, String>> {
		@Override
		public Map<String, String> compute(SakerFile file) throws IOException {
			Map<String, String> urlvals = new TreeMap<>();
			String content = file.getContent();
			String[] lines = content.split("[\n\r]+");
			for (int i = 0; i < lines.length; i++) {
				String s = lines[i];
				if (s.trim().isEmpty()) {
					continue;
				}
				s = s.trim();
				if (i + 1 >= lines.length) {
					break;
				}
				urlvals.put(s, lines[++i].trim());
			}
			return urlvals;
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
}
