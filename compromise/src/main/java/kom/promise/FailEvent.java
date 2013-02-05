package kom.promise;

import kom.events.Event;

public final class FailEvent extends PromiseResult<Object> implements Event {

    public FailEvent(Object data) {
        super(data);
    }
}
