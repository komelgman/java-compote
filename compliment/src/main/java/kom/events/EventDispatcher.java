package kom.events;


import kom.util.Callback;
import kom.util.CallbackExecutor;

/**
 * User: komelgman
 * Date: 8/31/12
 * Time: 1:05 PM
 */
public interface EventDispatcher<T extends Event> {
    public <Y extends T> void addEventListener(Class<Y> eventType, Callback<? super Y> listener);
    public void removeEventListener(Class<? extends T> eventType);
    public <Y extends T> void removeEventListener(Class<Y> eventType, Callback<? super Y> listener);

    public void dispatchEvent(T event);
}
