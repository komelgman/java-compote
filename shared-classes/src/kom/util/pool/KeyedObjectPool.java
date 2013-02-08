package kom.util.pool;

public interface KeyedObjectPool<T> extends ObjectPool<T> {
    public <A extends T> A getObject(Class<A> klass);
}
