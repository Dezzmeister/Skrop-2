package processing.test.skropclient.network;

import java.util.concurrent.atomic.AtomicBoolean;

public class RecentPasser<T> {

    private T received = null;
    private final AtomicBoolean retrieved = new AtomicBoolean(false);
    private final AtomicBoolean changed = new AtomicBoolean(false);

    /**
     * Returns <code>true</code> if this <code>RecentPasser</code> has a new object
     * of type <code>T</code>. Should only return true <b>ONCE</b> for a new object.
     *
     * @return <code>true</code> if there is a new object
     */
    public synchronized boolean hasNew() {

        return changed.getAndSet(false);
    }

    /**
     * Updates this Passer's object.
     *
     * @param object
     *            object to be passed
     */
    public void pass(T object) {
        received = object;

        boolean expected = retrieved.get();

        while (!retrieved.compareAndSet(expected, false));

        expected = changed.get();

        while (!changed.compareAndSet(expected, true));
    }

    /**
     * Returns the current object of type <code>T</code> held by this
     * <code>RecentPasser</code>.
     *
     * @return object to be passed
     */
    public T retrieve() {

        boolean expected = retrieved.get();

        while (!retrieved.compareAndSet(expected, true));

        return received;
    }

    /**
     * Returns <code>true</code> if any thread has retrieved the current object.
     *
     * @return <code>true</code> if this <code>RecentPasser</code>'s object has been
     *         retrieved
     */
    public boolean retrieved() {
        return retrieved.get();
    }
}
