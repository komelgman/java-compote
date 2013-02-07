package kom.util.pool;

import java.util.ArrayList;

/**
 * User: syungman
 * Date: 06.02.13
 */
public class SimpleObjectPool<T extends Poolable> implements ObjectPool<T> {

    private final int maxIndex;
    private final Class<T> klass;
    private final ArrayList<T> container;
    private volatile int currentIndex = -1;

    public SimpleObjectPool(int maxCapacity, Class<T> klass) {
        this.maxIndex = maxCapacity - 1;
        this.klass = klass;
        container = new ArrayList<T>(maxCapacity);
    }

    public synchronized T getObject() {
        T result = currentIndex < 0
                ? createPoolableObject()
                : extractPoolableObject();

        result.setOwnerPool(this);

        return result;
    }

    private T extractPoolableObject() {
        return container.remove(currentIndex--);
    }

    private T createPoolableObject() {
        try {
            return klass.newInstance();
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public synchronized void returnObject(T object) {
        if (currentIndex < maxIndex) {
            container.set(++currentIndex, object);
            object.setOwnerPool(null);
        }
    }
}
