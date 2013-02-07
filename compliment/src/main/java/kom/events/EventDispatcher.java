package kom.events;


import kom.util.callback.Callback;

public interface EventDispatcher<T extends Event> {
    public <Y extends T> void addEventListener(Class<Y> eventType, Callback<? super Y> listener);
    public void removeEventListener(Class<? extends T> eventType);
    public void removeEventListeners();
    public <Y extends T> void removeEventListener(Class<Y> eventType, Callback<? super Y> listener);

    public void dispatchEvent(T event);
}
