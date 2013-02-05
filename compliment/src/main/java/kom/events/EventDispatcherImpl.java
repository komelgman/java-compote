package kom.events;

import kom.util.Callback;
import kom.util.CallbackExecutor;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

public class EventDispatcherImpl<T extends Event> implements EventDispatcher<T> {

    private final Map<Class<? extends Event>, List<Callback<Event>>>
            listenersMap = new ConcurrentHashMap<Class<? extends Event>, List<Callback<Event>>>();

    private CallbackExecutor executor = new CallbackExecutor();

    private ThreadPoolExecutor threadPool;

    @Override
    @SuppressWarnings("unchecked")
    public <Y extends T> void addEventListener(Class<Y> eventType, Callback<? super Y> listener) {
        List<Callback<Event>> listeners = listenersMap.get(eventType);

        if (listeners == null) {
            synchronized (listenersMap) {
                if (!listenersMap.containsKey(eventType)) {
                    listenersMap.put(eventType, Collections.synchronizedList(new ArrayList<Callback<Event>>()));
                }
            }

            listeners = listenersMap.get(eventType);
        }

        listeners.add((Callback<Event>)listener);
    }

    @Override
    public void removeEventListener(Class<? extends T> eventType) {
        listenersMap.remove(eventType);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <Y extends T> void removeEventListener(Class<Y> eventType, Callback<? super Y> listener) {
        List<Callback<Event>> listeners = listenersMap.get(eventType);

        if (listeners != null) {
            listeners.remove((Callback<Event>)listener);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
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

        for (Callback<Event> listener : listeners) {
            executor.execute(listener, event);
        }
    }

    public void setThreadPool(Executor threadPool) {
        executor.setThreadPool(threadPool);
    }
}