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

package kom.util.collections;

import kom.util.callback.Callback;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ConcurrentMultiMap<Key, Value> {
    private final ConcurrentMap<Key, ConcurrentLinkedQueue<Value>> values
            = new ConcurrentHashMap<Key, ConcurrentLinkedQueue<Value>>();

    private final ConcurrentLinkedQueue<Value> emptyQueue = new ConcurrentLinkedQueue<Value>();

    public void add(Key key, Value value) {
        Queue<Value> queue = values.get(key);

        if (queue == null) {
            final ConcurrentLinkedQueue<Value> newList = new ConcurrentLinkedQueue<Value>();
            final ConcurrentLinkedQueue<Value> oldList = values.putIfAbsent(key, newList);

            if (oldList == null) {
                queue = newList;
            } else {
                queue = oldList;
            }
        }

        queue.add(value);
    }

    public Queue<Value> remove(Key key) {
        return values.remove(key);
    }

    public boolean remove(Key key, Value value) {
        final Queue<Value> queue = values.get(key);

        if (queue == null) {
            return false;
        }

        try {
            return queue.remove(value);
        } finally {
            values.remove(key, emptyQueue);
        }
    }

    public Iterable<Value> values(Key key) {
        final Queue<Value> queue = values.get(key);

        if (queue == null) {
            return Collections.emptyList();
        }

        return Collections.unmodifiableCollection(queue);
    }

    public void foreach(Key key, Callback<Value> function) {
        final Queue<Value> queue = values.get(key);

        if (queue == null) {
            return;
        }

        for (Value value : queue) {
            function.handle(value);
        }
    }

    public void removeAll() {
        values.clear();
    }
}