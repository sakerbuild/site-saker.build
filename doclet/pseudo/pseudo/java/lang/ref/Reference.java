package pseudo.java.lang.ref;

/**
 * Abstract base class for reference objects. This class defines the operations common to all reference objects. Because
 * reference objects are implemented in close cooperation with the garbage collector, this class may not be subclassed
 * directly.
 *
 * @author Mark Reinhold
 * @since 1.2
 */

public abstract class Reference<T> {

	/**
	 * Returns this reference object's referent. If this reference object has been cleared, either by the program or by
	 * the garbage collector, then this method returns <code>null</code>.
	 *
	 * @return The object to which this reference refers, or <code>null</code> if this reference object has been cleared
	 */
	public T get() {
		throw new UnsupportedOperationException();
	}

	/**
	 * Clears this reference object. Invoking this method will not cause this object to be enqueued.
	 * <p>
	 * This method is invoked only by Java code; when the garbage collector clears references it does so directly,
	 * without invoking this method.
	 */
	public void clear() {
		throw new UnsupportedOperationException();
	}

	/**
	 * Tells whether or not this reference object has been enqueued, either by the program or by the garbage collector.
	 * If this reference object was not registered with a queue when it was created, then this method will always return
	 * <code>false</code>.
	 *
	 * @return <code>true</code> if and only if this reference object has been enqueued
	 */
	public boolean isEnqueued() {
		throw new UnsupportedOperationException();
	}

	/**
	 * Adds this reference object to the queue with which it is registered, if any.
	 * <p>
	 * This method is invoked only by Java code; when the garbage collector enqueues references it does so directly,
	 * without invoking this method.
	 *
	 * @return <code>true</code> if this reference object was successfully enqueued; <code>false</code> if it was
	 *             already enqueued or if it was not registered with a queue when it was created
	 */
	public boolean enqueue() {
		throw new UnsupportedOperationException();
	}

}
