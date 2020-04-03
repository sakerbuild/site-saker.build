package example;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import saker.build.file.content.ContentDescriptor;

public class OddNumbersContentDescriptor 
		implements ContentDescriptor, Externalizable {
	private int max;

	/**
	 * For {@link Externalizable}.
	 */
	public OddNumbersContentDescriptor() {
	}

	public OddNumbersContentDescriptor(int max) {
		this.max = max;
	}

	public int getMax() {
		return max;
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeInt(max);
	}

	@Override
	public void readExternal(ObjectInput in) 
			throws IOException, ClassNotFoundException {
		max = in.readInt();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + max;
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
		OddNumbersContentDescriptor other = (OddNumbersContentDescriptor) obj;
		if (max != other.max)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "OddNumbersContentDescriptor[max=" + max + "]";
	}
}
