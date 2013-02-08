package kom.promise;

import kom.promise.events.PromiseEvent;
import kom.util.callback.CallbackExecutor;
import kom.util.pool.*;

import java.util.Timer;

@SuppressWarnings("UnusedDeclaration")
public class PromiseEnvironment {
    private static final Timer scheduler = new Timer(true);

    private final SimpleObjectPool<Promise> promisePool;
    private final KeyedObjectPool<PromiseEvent> eventPool;
    private final CallbackExecutor executor;

    public PromiseEnvironment(SimpleObjectPool<Promise> promisePool, KeyedObjectPool<PromiseEvent> eventPool,
                              CallbackExecutor executor) {
        this.promisePool = promisePool;
        this.eventPool = eventPool;
        this.executor = executor;
    }

    public SimpleObjectPool<Promise> getPromisePool() {
        return promisePool;
    }

    public KeyedObjectPool<PromiseEvent> getEventPool() {
        return eventPool;
    }

    public CallbackExecutor getCallbackExecutor() {
        return executor;
    }

    public Timer getScheduler() {
        return scheduler;
    }

    public static PromiseEnvironment getDefaultEnvironment() {
        return SingletonHolder.HOLDER_INSTANCE;
    }

    private static class SingletonHolder {
        public static final PromiseEnvironment HOLDER_INSTANCE
                = new PromiseEnvironment(
                    new SimpleObjectPoolImpl<Promise>(512, Promise.class),
                    new KeyedObjectPoolImpl<PromiseEvent>(1024),
                    null);
    }
}