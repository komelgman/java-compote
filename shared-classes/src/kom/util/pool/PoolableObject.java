package kom.util.pool;

@SuppressWarnings("unchecked")
public class PoolableObject implements Poolable {

    private ObjectPool owner;

    @Override
    public void setOwnerPool(ObjectPool owner) {
        this.owner = owner;
    }

    @Override
    public void release() {
        if (owner != null)
            owner.returnObject(this);
    }
}
