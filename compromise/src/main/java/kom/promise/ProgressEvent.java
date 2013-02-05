package kom.promise;

import kom.events.Event;

public final class ProgressEvent extends PromiseResult<Object> implements Event {
    public ProgressEvent(Object data) {
        super(data);
    }
}
