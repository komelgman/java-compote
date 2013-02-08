package kom.util.pool;

public interface Poolable {
    public void setOwnerPool(ObjectPool owner);
    public void release();
}
