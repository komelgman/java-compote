package kom.util.pool;

/**
 * User: syungman
 * Date: 06.02.13
 */
public interface Poolable {
    public void setOwnerPool(ObjectPool owner);
    public void release();
}
