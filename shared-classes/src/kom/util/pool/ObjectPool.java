package kom.util.pool;

/**
 * User: syungman
 * Date: 06.02.13
 */
public interface ObjectPool<T> {
    public void returnObject(T object);
}
