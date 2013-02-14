/*
 * Copyright 2013 Sergey Yungman (aka komelgman)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package kom.events;

import kom.util.callback.Callback;
import kom.util.callback.CallbackExecutor;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

@SuppressWarnings("unchecked")
public class EventDispatcherImpl<T extends Event> implements EventDispatcher<T> {
    private static final Logger log = Logger.getLogger(EventDispatcherImpl.class.getName());

    private static final Map<Class<? extends Event>, List<Class<? extends Event>>> eventsCache
            = new ConcurrentHashMap<Class<? extends Event>, List<Class<? extends Event>>>();

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

        listeners.add((Callback<Event>) listener);
    }

    @Override
    public void removeEventListener(Class<? extends T> eventType) {
        listenersMap.remove(eventType);
    }

    @Override
    @SuppressWarnings("RedundantCast")
    public <Y extends T> void removeEventListener(Class<Y> eventType, Callback<? super Y> listener) {
        List<Callback<Event>> listeners = listenersMap.get(eventType);

        if (listeners != null) {
            listeners.remove((Callback<Event>) listener);
        }
    }

    @Override
    public void removeEventListeners() {
        listenersMap.clear();
    }

    @Override
    public void dispatchEvent(T event) {
        final Class<? extends Event> rootEventType = event.getClass();
        List<Class<? extends Event>> eventTypesList = eventsCache.get(rootEventType);

        if (eventTypesList == null) {
            synchronized (eventsCache) {
                if (eventsCache.containsKey(rootEventType)) {
                    eventTypesList = eventsCache.get(rootEventType);
                } else {
                    eventTypesList = getEvents(rootEventType);
                    eventsCache.put(rootEventType, eventTypesList);
                }
            }
        }

        for (Class<? extends Event> eventType : eventTypesList) {
            dispatchEvent(eventType, event);
        }
    }

    private List<Class<? extends Event>> getEvents(Class<? extends Event> fromEventType) {
        final ArrayList<Class<? extends Event>> result = new ArrayList<Class<? extends Event>>();
        final LinkedList<Class<?>> events = new LinkedList<Class<?>>();
        events.addFirst(fromEventType);

        while (!events.isEmpty()) {
            final Class<?> eventType = events.removeLast();

            if (eventType != null && Event.class.isAssignableFrom(eventType)) {
                if (result.contains((Class<? extends Event>) eventType)) {
                    continue;
                }

                result.add((Class<? extends Event>) eventType);
                events.addFirst(eventType.getSuperclass());

                for (Class<?> item : eventType.getInterfaces()) {
                    events.addFirst(item);
                }
            }
        }

        return result;
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
            executeCallback(event, listener);
        }
    }

    private void executeCallback(T event, Callback<Event> listener) {
        try {
            listener.handle(event);
        } catch (Exception e) {
            log.log(Level.WARNING, e.getMessage(), e);
        }
    }

    public void setCallbackExecutor(CallbackExecutor executor) {
        this.executor = executor;
    }
}