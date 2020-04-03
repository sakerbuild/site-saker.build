package saker.docgen;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import saker.build.file.path.SakerPath;
import saker.build.task.identifier.TaskIdentifier;

public class DocWorkerTaskIdentifier implements TaskIdentifier, Externalizable {
	private static final long serialVersionUID = 1L;

	private SakerPath outputDirectory;

	/**
	 * For {@link Externalizable}.
	 */
	public DocWorkerTaskIdentifier() {
	}

	public DocWorkerTaskIdentifier(SakerPath outputDirectory) {
		this.outputDirectory = outputDirectory;
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeObject(outputDirectory);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		outputDirectory = (SakerPath) in.readObject();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((outputDirectory == null) ? 0 : outputDirectory.hashCode());
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
		DocWorkerTaskIdentifier other = (DocWorkerTaskIdentifier) obj;
		if (outputDirectory == null) {
			if (other.outputDirectory != null)
				return false;
		} else if (!outputDirectory.equals(other.outputDirectory))
			return false;
		return true;
	}

}