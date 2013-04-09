package kom.util.collections;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * User: syungman
 * Date: 09.04.13
 */
public class ConcurrentMapOfList<Key, Value> {
    private final ConcurrentMap<Key, ConcurrentList<Value>> values
            = new ConcurrentHashMap<Key, ConcurrentList<Value>>();

    public void add(Key key, Value value) {
        List<Value> list = values.get(key);

        if (list == null) {
            final ConcurrentList<Value> newList = new ConcurrentList<Value>();
            final ConcurrentList<Value> oldList = values.putIfAbsent(key, newList);

            if (oldList == null) {
                list = newList;
            } else {
                list = oldList;
            }
        }

        list.add(value);
    }

    public List<Value> remove(Key key) {
        return values.remove(key);
    }

    public boolean remove(Key key, Value value) {
        final List<Value> list = values.get(key);

        if (list == null) {
            return false;
        }

        return list.remove(value);
    }

    public List<Value> obtainListCopy(Key key) {
        final ConcurrentList<Value> list = values.get(key);

        if (list == null) {
            return Collections.emptyList();
        }

        return list.clone();
    }

    public void removeAll() {
        values.clear();
    }

    private class ConcurrentList<V> extends ArrayList<V> {
        final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
        final Lock readLock = lock.readLock();
        final Lock writeLock = lock.writeLock();

        @Override
        public boolean add(V v) {
            writeLock.lock();
            try {
                return super.add(v);
            }finally {
                writeLock.unlock();
            }
        }

        @Override
        public V remove(int index) {
            writeLock.lock();
            try {
                return super.remove(index);
            }finally {
                writeLock.unlock();
            }
        }

        @Override
        public ConcurrentList<V> clone() {
            readLock.lock();
            try {
                return (ConcurrentList<V>)super.clone();
            }finally {
                readLock.unlock();
            }
        }
    }
}