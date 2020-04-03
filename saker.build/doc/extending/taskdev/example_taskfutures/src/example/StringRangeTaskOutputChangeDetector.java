package example;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Objects;

import saker.build.task.dependencies.TaskOutputChangeDetector;

public class StringRangeTaskOutputChangeDetector implements TaskOutputChangeDetector, Externalizable {
	private static final long serialVersionUID = 1L;

	private String expectedRangeStr;

	private int start;
	private int end;

	/**
	 * For {@link Externalizable}.
	 */
	public StringRangeTaskOutputChangeDetector() {
	}

	public StringRangeTaskOutputChangeDetector(String expectedRangeStr, int start, int end) {
		this.expectedRangeStr = expectedRangeStr;
		this.start = start;
		this.end = end;
	}

	@Override
	public boolean isChanged(Object taskoutput) {
		// simplified implementation
		String taskstr = Objects.toString(taskoutput);
		return !expectedRangeStr.equals(taskstr.substring(start, end));
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeUTF(expectedRangeStr);
		out.writeInt(start);
		out.writeInt(end);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		expectedRangeStr = in.readUTF();
		start = in.readInt();
		end = in.readInt();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + end;
		result = prime * result + start;
		result = prime * result + ((expectedRangeStr == null) ? 0 : expectedRangeStr.hashCode());
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
		StringRangeTaskOutputChangeDetector other = (StringRangeTaskOutputChangeDetector) obj;
		if (end != other.end)
			return false;
		if (start != other.start)
			return false;
		if (expectedRangeStr == null) {
			if (other.expectedRangeStr != null)
				return false;
		} else if (!expectedRangeStr.equals(other.expectedRangeStr))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "StringRangeTaskOutputChangeDetector[" + (expectedRangeStr != null ? "str=" + expectedRangeStr + ", " : "") + "start=" + start + ", end=" + end
				+ "]";
	}
}
