package saker.docgen;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Supplier;

import saker.build.thirdparty.saker.util.StringUtils;

class ProcessedTemplate {
	private static class Region {
		private CharSequence content;
		private PlaceholderType type;

		public Region(CharSequence content, PlaceholderType type) {
			this.content = content;
			this.type = type;
		}

		public CharSequence getContent() {
			return content;
		}

		public PlaceholderType getType() {
			return type;
		}
	}

	private List<ProcessedTemplate.Region> regions = new ArrayList<>();

	public ProcessedTemplate(String template, PlaceholderCollection placeholders,
			Iterable<Entry<String, String>> macros) {
		Map<PlaceholderType, String> typedplaceholders = new EnumMap<>(PlaceholderType.class);
		if (placeholders != null) {
			addPlaceholderType(placeholders.getBody(), PlaceholderType.BODY, typedplaceholders);
			addPlaceholderType(placeholders.getNavigation(), PlaceholderType.NAVIGATION, typedplaceholders);
			addPlaceholderType(placeholders.getTitle(), PlaceholderType.TITLE, typedplaceholders);
			addPlaceholderType(placeholders.getHeadingsNavigation(), PlaceholderType.HEADINGS_NAVIGATION,
					typedplaceholders);
			addPlaceholderType(placeholders.getJavaScriptLanguagesArray(), PlaceholderType.JS_LANGUAGES_ARRAY,
					typedplaceholders);
			addPlaceholderType(placeholders.getPreviousSection(), PlaceholderType.PREV_SECTION, typedplaceholders);
			addPlaceholderType(placeholders.getNextSection(), PlaceholderType.NEXT_SECTION, typedplaceholders);
			addPlaceholderType(placeholders.getLanguageScripts(), PlaceholderType.LANGUAGE_SCRIPTS, typedplaceholders);
			addPlaceholderType(placeholders.getPathToRootDir(), PlaceholderType.PATH_TO_ROOT_DIR, typedplaceholders);
		}

		int len = template.length();
		int lastend = 0;
		outer:
		for (int i = 0; i < len;) {
			for (Entry<PlaceholderType, String> entry : typedplaceholders.entrySet()) {
				String pl = entry.getValue();
				if (template.startsWith(pl, i)) {
					if (lastend < i) {
						regions.add(new Region(StringUtils.subCharSequence(template, lastend, i - lastend), null));
					}
					regions.add(new Region(null, entry.getKey()));
					i += pl.length();
					lastend = i;
					continue outer;
				}
			}
			for (Entry<String, String> entry : macros) {
				String m = entry.getKey();
				if (template.startsWith(m, i)) {
					if (lastend < i) {
						regions.add(new Region(StringUtils.subCharSequence(template, lastend, i - lastend), null));
					}
					regions.add(new Region(entry.getValue(), null));
					i += m.length();
					lastend = i;
					continue outer;
				}
			}
			i++;
		}
		if (lastend < len) {
			regions.add(new Region(StringUtils.subCharSequence(template, lastend, len - lastend), null));
		}
	}

	private static void addPlaceholderType(String placeholder, PlaceholderType type,
			Map<PlaceholderType, String> typedplaceholders) {
		if (placeholder != null) {
			typedplaceholders.put(type, placeholder);
		}
	}

	public String process(Map<PlaceholderType, ? extends Supplier<? extends CharSequence>> placeholdercontents) {
		StringBuilder sb = new StringBuilder();
		for (ProcessedTemplate.Region r : regions) {
			PlaceholderType type = r.getType();
			if (type == null) {
				sb.append(r.getContent());
			} else {
				Supplier<? extends CharSequence> contentsupplier = placeholdercontents.get(type);
				if (contentsupplier != null) {
					sb.append(contentsupplier.get());
				}
			}
		}
		return sb.toString();
	}
}