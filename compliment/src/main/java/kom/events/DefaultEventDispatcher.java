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
import kom.util.collections.ConcurrentMultiMap;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


public class DefaultEventDispatcher<T> implements EventDispatcher<T> {
    private static final Map<Class, List<Class>> eventsCache = new ConcurrentHashMap<Class, List<Class>>();

    private final ConcurrentMultiMap<Class, Callback<T>> listenersMap = new ConcurrentMultiMap<Class, Callback<T>>();

    private final Class<T> genericType;
    private CallbackExecutor executor;

    public DefaultEventDispatcher(Class<T> genericType) {
        this.genericType = genericType;
    }

    @Override
    public void addEventListener(Callback<T> listener) {
        addEventListener(genericType, listener);
    }

    @Override
    public <Y extends T> void addEventListener(Class<Y> eventType, Callback<? super Y> listener) {
        //noinspection unchecked
        listenersMap.add(eventType, (Callback<T>) listener);
    }

    @Override
    public <Y extends T> void removeEventListener(Class<Y> eventType, Callback<? super Y> listener) {
        //noinspection unchecked
        listenersMap.remove(eventType, (Callback<T>)listener);
    }

    @Override
    public void removeEventListeners(Class<? extends T> eventType) {
        listenersMap.remove(eventType);
    }

    @Override
    public void removeEventListener(Callback<T> listener) {
        removeEventListener(genericType, listener);
    }

    @Override
    public void removeEventListeners() {
        listenersMap.removeAll();
    }

    @Override
    public void dispatchEvent(T event) {
        final Class rootEventType = event.getClass();
        List<Class> eventTypesList = eventsCache.get(rootEventType);

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

        for (Class eventType : eventTypesList) {
            dispatchEvent(eventType, event);
        }
    }

    private List<Class> getEvents(Class fromEventType) {
        final ArrayList<Class> result = new ArrayList<Class>();
        final LinkedList<Class> events = new LinkedList<Class>();
        events.addFirst(fromEventType);

        while (!events.isEmpty()) {
            final Class eventType = events.removeLast();

            if (eventType != null && (genericType.isAssignableFrom(eventType)) ) {
                if (result.contains(eventType)) {
                    continue;
                }

                result.add(eventType);
                events.addFirst(eventType.getSuperclass());

                for (Class item : eventType.getInterfaces()) {
                    events.addFirst(item);
                }
            }
        }

        return result;
    }

    protected void dispatchEvent(Class eventType, final T event) {
        if (executor == null) {
            executor = RunnableCallbackExecutor.getInstance();
        }

        listenersMap.foreach(eventType, new Callback<Callback<T>>() {
            @Override
            public void handle(Callback<T> listener) {
                executor.execute(listener, event);
            }
        });
    }

    public void setCallbackExecutor(CallbackExecutor executor) {
        this.executor = executor;
    }
}