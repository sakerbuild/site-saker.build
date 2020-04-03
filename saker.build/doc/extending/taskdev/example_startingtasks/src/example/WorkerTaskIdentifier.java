package example;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import saker.build.file.path.SakerPath;
import saker.build.task.identifier.TaskIdentifier;

public class WorkerTaskIdentifier implements TaskIdentifier, Externalizable {

	private SakerPath sourcePath;

	/**
	 * For {@link Externalizable}.
	 */
	public WorkerTaskIdentifier() {
	}

	public WorkerTaskIdentifier(SakerPath sourcePath) {
		this.sourcePath = sourcePath;
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeObject(sourcePath);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		sourcePath = (SakerPath) in.readObject();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((sourcePath == null) ? 0 : sourcePath.hashCode());
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
		WorkerTaskIdentifier other = (WorkerTaskIdentifier) obj;
		if (sourcePath == null) {
			if (other.sourcePath != null)
				return false;
		} else if (!sourcePath.equals(other.sourcePath))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "WorkerTaskIdentifier[" + sourcePath + "]";
	}

}
