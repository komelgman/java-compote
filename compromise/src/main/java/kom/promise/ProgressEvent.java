package kom.promise;

import kom.events.Event;

/**
 * User: syungman
 * Date: 04.02.13
 */
public final class ProgressEvent extends PromiseResult<Object> implements Event {
    public ProgressEvent(Object data) {
        super(data);
    }
}
