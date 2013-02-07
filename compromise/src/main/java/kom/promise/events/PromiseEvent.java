package kom.promise.events;

import kom.events.Event;
import kom.util.pool.PoolableObject;

public class PromiseEvent<T> extends PoolableObject implements Event {
    private T data;

    public void setData(T data) {
        this.data = data;
    }

    public T getData() {
        return data;
    }

    @Override
    public void release() {
        data = null;
        super.release();
    }
}
