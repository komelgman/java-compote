package kom.util.pool;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


@SuppressWarnings({"unchecked", "SuspiciousMethodCalls"})
public class KeyedObjectPoolImpl<T extends Poolable> implements KeyedObjectPool<T> {
    private final int maxCapacity;
    private final Map<Class<?extends T>, SimpleObjectPoolImpl<?>> map;

    private int size = 0;

    public KeyedObjectPoolImpl(int maxCapacity) {
        this.maxCapacity = maxCapacity;
        this.map = new ConcurrentHashMap<Class<? extends T>, SimpleObjectPoolImpl<?>>();
    }

    public <Y extends T> Y getObject(Class<Y> klass) {
        SimpleObjectPoolImpl<Y> pool = (SimpleObjectPoolImpl<Y>)map.get(klass);

        if (pool == null) {
            synchronized (map) {
                if (map.containsKey(klass)) {
                    pool = (SimpleObjectPoolImpl<Y>)map.get(klass);
                } else {
                    pool = new SimpleObjectPoolImpl<Y>(maxCapacity, klass);
                    map.put(klass, pool);
                }
            }
        }

        Y object = pool.getObject();
        object.setOwnerPool(this);
        size = Math.max(0, --size);

        return object;
    }

    @Override
    public void returnObject(T object) {
        if (size < maxCapacity) {
            final SimpleObjectPoolImpl pool = map.get(object.getClass());
            if (pool == null) {
                throw new IllegalStateException("ObjectPool for this object class was not initialized");
            }

            pool.returnObject(object);
        }
    }
}