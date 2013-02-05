package kom.promise;

import kom.events.Event;

/**
 * User: syungman
 * Date: 04.02.13
 */
public final class CancelEvent extends PromiseResult<Object> implements Event {

    public CancelEvent(Object data) {
        super(data);
    }
}
