package kom.promise.events;

import kom.events.Event;
import kom.util.PoolableObject;

public class PromiseEvent extends PoolableObject implements Event {
    private Object data;

    public void setData(Object data) {
        this.data = data;
    }

    public Object getData() {
        return data;
    }

    @Override
    public void release() {
        data = null;
        super.release();
    }
}
