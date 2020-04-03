package example;

import java.io.IOException;
import java.io.OutputStream;

import saker.build.exception.InvalidPathFormatException;
import saker.build.file.SakerFileBase;
import saker.build.file.content.ContentDescriptor;

public class OddNumbersSakerFile extends SakerFileBase {
	private OddNumbersContentDescriptor contentDescriptor;

	public OddNumbersSakerFile(String name, int max) {
		super(name);
		this.contentDescriptor = new OddNumbersContentDescriptor(max);
	}

	@Override
	public ContentDescriptor getContentDescriptor() {
		return contentDescriptor;
	}

	@Override
	public void writeToStreamImpl(OutputStream os) 
			throws IOException {
		int max = contentDescriptor.getMax();
		for (int i = 1; i < max; i += 2) {
			os.write(Integer.toString(i).getBytes());
			os.write('\n');
		}
	}

	@Override
	public int getEfficientOpeningMethods() {
		return OPENING_METHODS_ALL;
	}
}
