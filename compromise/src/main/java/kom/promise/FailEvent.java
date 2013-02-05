package kom.promise;

import kom.events.Event;

/**
 * User: syungman
 * Date: 04.02.13
 */
public final class FailEvent extends PromiseResult<Object> implements Event {

    public FailEvent(Object data) {
        super(data);
    }
}
