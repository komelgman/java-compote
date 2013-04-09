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
import kom.util.callback.RunnableCallbackExecutor;
import kom.util.collections.ConcurrentMapOfList;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

@SuppressWarnings("unchecked")
public class DefaultEventDispatcher<T extends Event> implements EventDispatcher<T> {
    private static final Map<Class<? extends Event>, List<Class<? extends Event>>> eventsCache
            = new ConcurrentHashMap<Class<? extends Event>, List<Class<? extends Event>>>();

    private final ConcurrentMapOfList<Class<? extends Event>, Callback<Event>>
            listenersMap = new ConcurrentMapOfList<Class<? extends Event>, Callback<Event>>();

    private CallbackExecutor executor;

    @Override
    public <Y extends T> void addEventListener(Class<Y> eventType, Callback<? super Y> listener) {
        listenersMap.add(eventType, (Callback<Event>) listener);
    }

    @Override
    public <Y extends T> void removeEventListener(Class<Y> eventType, Callback<? super Y> listener) {
        listenersMap.remove(eventType, (Callback<Event>)listener);
    }

    @Override
    public void removeEventListeners(Class<? extends T> eventType) {
        listenersMap.remove(eventType);
    }

    @Override
    public void removeEventListeners() {
        listenersMap.removeAll();
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
        if (executor == null) {
            executor = RunnableCallbackExecutor.getInstance();
        }

        for (Callback<Event> listener : listenersMap.obtainListCopy(eventType)) {
            executor.execute(listener, event);
        }
    }

    public void setCallbackExecutor(CallbackExecutor executor) {
        this.executor = executor;
    }
}