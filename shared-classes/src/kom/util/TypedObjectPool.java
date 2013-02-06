package kom.util;

/**
 * User: syungman
 * Date: 06.02.13
 */
public class TypedObjectPool<T extends Poolable> implements ObjectPool<T> {


    public TypedObjectPool(int maxCapacity) {

    }

    public T getObject(Class<? extends T> klass) {
        // stub
        return createPoolableObject(klass);
    }

    private T createPoolableObject(Class<? extends T> klass) {
        try {
            T result = klass.newInstance();
            result.setOwnerPool(this);
            return result;
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public void returnObject(T object) {
        // stub
    }
}