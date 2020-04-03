package saker.nest.taskdoc;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NavigableSet;

import saker.build.thirdparty.saker.util.ObjectUtils;
import saker.build.thirdparty.saker.util.StringUtils;

public class TemplatedContents {
	private interface Region {
		public void write(Appendable out, Map<String, String> templatekeywords) throws IOException;
	}

	private static class StringRegion implements Region {
		CharSequence contents;

		public StringRegion(CharSequence contents) {
			this.contents = contents;
		}

		@Override
		public void write(Appendable out, Map<String, String> templatekeywords) throws IOException {
			out.append(contents);
		}
	}

	private static class TemplatedRegion implements Region {
		String keyword;

		public TemplatedRegion(String keyword) {
			this.keyword = keyword;
		}

		@Override
		public void write(Appendable out, Map<String, String> templatekeywords) throws IOException {
			String contents = templatekeywords.get(keyword);
			if (contents == null) {
				throw new IllegalArgumentException("Template contents missing for keyword: " + keyword);
			}
			out.append(contents);
		}
	}

	private List<Region> regions = new ArrayList<>();

	public TemplatedContents(Collection<String> keywords) {
		for (String kw : keywords) {
			regions.add(new TemplatedRegion(kw));
		}
	}

	public TemplatedContents(String template, NavigableSet<String> keywords) {
		ObjectUtils.requireNaturalOrder(keywords);

		int len = template.length();
		int lastend = 0;
		if (!ObjectUtils.isNullOrEmpty(keywords)) {
			outer:
			for (int i = 0; i < len;) {
				for (Iterator<String> kwit = keywords.descendingIterator(); kwit.hasNext();) {
					String kw = kwit.next();
					if (template.startsWith(kw, i)) {
						if (lastend < i) {
							regions.add(new StringRegion(StringUtils.subCharSequence(template, lastend, i - lastend)));
						}
						regions.add(new TemplatedRegion(kw));
						i += kw.length();
						lastend = i;
						continue outer;
					}
				}
				i++;
			}
		}
		if (lastend < len) {
			regions.add(new StringRegion(StringUtils.subCharSequence(template, lastend, len - lastend)));
		}
	}

	public void writeTo(Appendable out, Map<String, String> templatekeywords) throws IOException {
		for (Region r : regions) {
			r.write(out, templatekeywords);
		}
	}
}
