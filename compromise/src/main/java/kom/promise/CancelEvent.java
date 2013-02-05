package kom.promise;

import kom.events.Event;

public final class CancelEvent extends PromiseResult<Object> implements Event {

    public CancelEvent(Object data) {
        super(data);
    }
}
