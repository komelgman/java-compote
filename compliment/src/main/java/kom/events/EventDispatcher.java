package kom.events;


/**
 * User: komelgman
 * Date: 8/31/12
 * Time: 1:05 PM
 */
public interface EventDispatcher<T extends Event> {
    public <Y extends T> void addEventListener(Class<Y> eventType, EventListener<? super Y> listener);
    public void removeEventListener(Class<? extends T> eventType);
    public <Y extends T> void removeEventListener(Class<Y> eventType, EventListener<? super Y> listener);
    public void dispatchEvent(T event);
    //public void dispatchEmptyEvent(Class<T> eventType);
    //public void dispatchEvent(Class<T> eventType, T event);
}
