package kom.promise;

import kom.events.Event;

/**
 * User: syungman
 * Date: 04.02.13
 */
public final class SuccessEvent<T> extends PromiseResult<T> implements Event {
    public SuccessEvent(T data) {
        super(data);
    }
}
