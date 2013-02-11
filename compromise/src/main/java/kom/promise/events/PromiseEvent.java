package kom.promise.events;

import kom.events.Event;

public class PromiseEvent<T> implements Event {
    private T data;

    public void setData(T data) {
        this.data = data;
    }

    public T getData() {
        return data;
    }
}
