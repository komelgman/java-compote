package kom.promise;

import kom.events.Event;

public final class SuccessEvent<T> extends PromiseResult<T> implements Event {
    public SuccessEvent(T data) {
        super(data);
    }
}
