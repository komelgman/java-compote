package kom.util.pool;

import java.util.ArrayList;

public class SimpleObjectPoolImpl<T extends Poolable> implements SimpleObjectPool<T> {

    private final int maxIndex;
    private final Class<T> klass;
    private final ArrayList<T> container;
    private volatile int currentIndex = -1;

    public SimpleObjectPoolImpl(int maxCapacity, Class<T> klass) {
        this.maxIndex = maxCapacity - 1;
        this.klass = klass;
        container = new ArrayList<T>(maxCapacity);

        for (int i = 0; i < maxCapacity; ++i) {
            container.add(i, null);
        }
    }

    @Override
    public synchronized T getObject() {
        T result = currentIndex < 0
                ? createPoolableObject()
                : extractPoolableObject();

        result.setOwnerPool(this);

        return result;
    }

    private T extractPoolableObject() {
        return container.set(currentIndex--, null);
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