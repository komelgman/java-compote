package kom.util.pool;

public interface ObjectPool<T> {
    public void returnObject(T object);
}
