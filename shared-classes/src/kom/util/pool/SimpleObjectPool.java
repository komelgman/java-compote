package kom.util.pool;

public interface SimpleObjectPool<T> extends ObjectPool<T> {
    public T getObject();
}
