package saker.nest.taskdoc;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import saker.build.file.path.SakerPath;

public final class SimpleTaskDocumentationTaskOutput implements TaskDocumentationTaskOutput, Externalizable {
	private static final long serialVersionUID = 1L;

	private SakerPath outputPath;

	/**
	 * For {@link Externalizable}.
	 */
	public SimpleTaskDocumentationTaskOutput() {
	}

	public void setOutputPath(SakerPath outputPath) {
		this.outputPath = outputPath;
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeObject(outputPath);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		outputPath = (SakerPath) in.readObject();
	}

	@Override
	public SakerPath getOutputPath() {
		return outputPath;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((outputPath == null) ? 0 : outputPath.hashCode());
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
		SimpleTaskDocumentationTaskOutput other = (SimpleTaskDocumentationTaskOutput) obj;
		if (outputPath == null) {
			if (other.outputPath != null)
				return false;
		} else if (!outputPath.equals(other.outputPath))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "SimpleTaskDocumentationTaskOutput[" + (outputPath != null ? "outputPath=" + outputPath : "") + "]";
	}

}