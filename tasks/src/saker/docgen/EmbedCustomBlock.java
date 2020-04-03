package saker.docgen;

import org.commonmark.node.CustomBlock;

import saker.build.file.path.SakerPath;
import saker.build.file.provider.SakerPathFiles;

public class EmbedCustomBlock extends CustomBlock {
	private SakerPath path;
	private String language;
	private boolean includeRaw;

	private Integer rangeStart = null;
	private Integer rangeEnd = null;
	private String rangeMarkerStart;
	private String rangeMarkerEnd;

	private boolean includeStartMarker;
	private boolean includeEndMarker;

	private boolean trimLineWhiteSpace;

	public EmbedCustomBlock(SakerPath path) {
		SakerPathFiles.requireRelativePath(path);
		this.path = path;
	}

	public boolean isIncludeRaw() {
		return includeRaw;
	}

	public SakerPath getPath() {
		return path;
	}

	public String getLanguage() {
		return language;
	}

	public Integer getRangeStart() {
		return rangeStart;
	}

	public Integer getRangeEnd() {
		return rangeEnd;
	}

	public String getRangeMarkerStart() {
		return rangeMarkerStart;
	}

	public String getRangeMarkerEnd() {
		return rangeMarkerEnd;
	}

	public void setIncludeRaw(boolean includeRaw) {
		this.includeRaw = includeRaw;
	}

	public void setLanguage(String language) {
		this.language = language;
	}

	public void setRangeStart(int rangeStart) {
		if (rangeStart == 0) {
			throw new IllegalArgumentException("Invalid start line: " + rangeStart);
		}
		this.rangeStart = rangeStart;
	}

	public void setRangeEnd(int rangeEnd) {
		if (rangeEnd == 0) {
			throw new IllegalArgumentException("Invalid end line: " + rangeEnd);
		}
		this.rangeEnd = rangeEnd;
	}

	public void setRangeMarkerStart(String rangemarkerstart) {
		this.rangeMarkerStart = rangemarkerstart;
	}

	public void setRangeMarkerEnd(String rangemarkerend) {
		this.rangeMarkerEnd = rangemarkerend;
	}

	public boolean isIncludeStartMarker() {
		return includeStartMarker;
	}

	public void setIncludeStartMarker(boolean includeStartMarker) {
		this.includeStartMarker = includeStartMarker;
	}

	public boolean isIncludeEndMarker() {
		return includeEndMarker;
	}

	public void setIncludeEndMarker(boolean includeEndMarker) {
		this.includeEndMarker = includeEndMarker;
	}

	public boolean isTrimLineWhiteSpace() {
		return trimLineWhiteSpace;
	}

	public void setTrimLineWhiteSpace(boolean trimLineWhiteSpace) {
		this.trimLineWhiteSpace = trimLineWhiteSpace;
	}

}