package kom.util.pool;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


@SuppressWarnings({"unchecked", "SuspiciousMethodCalls"})
public class TypedObjectPool<T extends Poolable> implements ObjectPool<T> {


    private final int maxCapacity;
    private final Map<Class<?extends T>, SimpleObjectPool<?>> map;

    private int size = 0;

    public TypedObjectPool(int maxCapacity) {
        this.maxCapacity = maxCapacity;
        this.map = new ConcurrentHashMap<Class<? extends T>, SimpleObjectPool<?>>();
    }

    public <Y extends T> Y getObject(Class<Y> klass) {
        SimpleObjectPool<Y> pool = (SimpleObjectPool<Y>)map.get(klass);

        if (pool == null) {
            synchronized (map) {
                if (map.containsKey(klass)) {
                    pool = (SimpleObjectPool<Y>)map.get(klass);
                } else {
                    pool = new SimpleObjectPool<Y>(maxCapacity, klass);
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
            final SimpleObjectPool pool = map.get(object.getClass());
            if (pool == null) {
                throw new IllegalStateException("ObjectPool for this object class was not initialized");
            }

            pool.returnObject(object);
        }
    }
}