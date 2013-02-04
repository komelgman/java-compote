package kom.events;

import kom.util.Callback;

/**
 * User: komelgman
 * Date: 8/31/12
 * Time: 1:09 PM
 */
public interface EventListener<T extends Event> extends Callback<T> {
    public void handle(T event);
}
