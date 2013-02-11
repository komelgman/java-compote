package kom.promise.util;

import kom.promise.Promise;
import kom.promise.events.PromiseEvent;
import kom.util.callback.Callback;
import kom.util.callback.CallbackExecutor;

import java.util.Timer;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

@SuppressWarnings("UnusedDeclaration")
public class PromiseEnvironment {
    private static final Timer scheduler = new Timer(true);

    private final CallbackExecutor callbackExecutor;
    private final Executor runnableExecutor;

    public PromiseEnvironment(Executor threadExecutor, CallbackExecutor executor) {
        this.runnableExecutor = (threadExecutor == null)
                ? Executors.newCachedThreadPool()
                : threadExecutor;

        this.callbackExecutor = executor;
    }

    public static PromiseEnvironment getDefaultEnvironment() {
        return SingletonHolder.HOLDER_INSTANCE;
    }

    public <T> void executeCallback(Callback<T> callback, T data) {
        if (callbackExecutor == null) {
            callback.handle(data);
        } else {
            callbackExecutor.execute(callback, data);
        }
    }

    public void executeRunnable(Runnable runnable) {
        runnableExecutor.execute(runnable);
    }

    public <T extends PromiseEvent> T getEvent(Class<T> reasonType) {
        try {
            return reasonType.newInstance();
        } catch (Exception e) {
            throw new IllegalStateException("Can't create new instance for Event object", e);
        }
    }

    public <T> Promise<T> getPromise() {
        Promise<T> promise = new Promise<T>();
        promise.setEnvironment(this);
        return promise;
    }

    public Timer getScheduler() {
        return scheduler;
    }

    public CallbackExecutor getCallbackExecutor() {
        return callbackExecutor;
    }

    public Executor getRunnableExecutor() {
        return runnableExecutor;
    }

    private static class SingletonHolder {
        public static final PromiseEnvironment HOLDER_INSTANCE = new PromiseEnvironment(null, null);
    }
}