package kom.util;

/**
 * User: syungman
 * Date: 06.02.13
 */
@SuppressWarnings("unchecked")
public class PoolableObject implements Poolable {

    private ObjectPool owner;

    @Override
    public void setOwnerPool(ObjectPool owner) {
        this.owner = owner;
    }

    @Override
    public void release() {
        owner.returnObject(this);
    }
}
