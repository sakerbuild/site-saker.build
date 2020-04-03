package saker.docgen;

import org.commonmark.node.CustomBlock;

import saker.build.file.path.SakerPath;
import saker.build.file.provider.SakerPathFiles;

public class IncludeCustomBlock extends CustomBlock {
	private SakerPath path;

	public IncludeCustomBlock(SakerPath path) {
		SakerPathFiles.requireAbsolutePath(path);
		this.path = path;
	}

	public SakerPath getPath() {
		return path;
	}
}