package kom.events;

import kom.util.callabck.Callback;
import kom.util.callabck.CallbackExecutor;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@SuppressWarnings("unchecked")
public class EventDispatcherImpl<T extends Event> implements EventDispatcher<T> {

    private final Map<Class<? extends Event>, List<Callback<Event>>>
            listenersMap = new ConcurrentHashMap<Class<? extends Event>, List<Callback<Event>>>();

    private CallbackExecutor executor;

    @Override
    public <Y extends T> void addEventListener(Class<Y> eventType, Callback<? super Y> listener) {
        List<Callback<Event>> listeners = listenersMap.get(eventType);

        if (listeners == null) {
            synchronized (listenersMap) {
                if (listenersMap.containsKey(eventType)) {
                    listeners = listenersMap.get(eventType);
                } else {
                    listeners = Collections.synchronizedList(new ArrayList<Callback<Event>>());
                    listenersMap.put(eventType, listeners);
                }
            }
        }

        listeners.add((Callback<Event>)listener);
    }

    @Override
    public void removeEventListener(Class<? extends T> eventType) {
        listenersMap.remove(eventType);
    }

    @Override
    public void removeEventListeners() {
        listenersMap.clear();
    }

    @Override
    @SuppressWarnings("RedundantCast")
    public <Y extends T> void removeEventListener(Class<Y> eventType, Callback<? super Y> listener) {
        List<Callback<Event>> listeners = listenersMap.get(eventType);

        if (listeners != null) {
            listeners.remove((Callback<Event>)listener);
        }
    }

    @Override
    public void dispatchEvent(T event) {
        // todo: need cache
        final LinkedList<Class<?>> events = new LinkedList<Class<?>>();
        events.addFirst(event.getClass());

        while (!events.isEmpty()) {
            Class<?> eventType = events.removeLast();

            if (eventType != null && Event.class.isAssignableFrom(eventType)) {
                dispatchEvent((Class<? extends Event>) eventType, event);

                events.addFirst(eventType.getSuperclass());

                for (Class<?> item : eventType.getInterfaces()) {
                    events.addFirst(item);
                }
            }
        }
    }

    protected void dispatchEvent(Class<? extends Event> eventType, T event) {
        List<Callback<Event>> listeners = listenersMap.get(eventType);

        if (listeners == null) {
            return;
        }

        if (executor == null) {
            manualHandleEvent(event, listeners);
        } else {
            handleEventOnExecutor(event, listeners);
        }
    }

    private void handleEventOnExecutor(T event, List<Callback<Event>> listeners) {
        for (Callback<Event> listener : listeners) {
            executor.execute(listener, event);
        }
    }

    private void manualHandleEvent(T event, List<Callback<Event>> listeners) {
        for (Callback<Event> listener : listeners) {
            listener.handle(event);
        }
    }

    public void setCallbackExecutor(CallbackExecutor executor) {
        this.executor = executor;
    }
}