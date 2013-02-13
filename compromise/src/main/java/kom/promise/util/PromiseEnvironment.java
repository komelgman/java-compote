package kom.promise.util;

import kom.promise.Promise;
import kom.promise.events.PromiseEvent;
import kom.util.callback.Callback;
import kom.util.callback.CallbackExecutor;
import kom.util.callback.RunnableCallbackExecutor;

import java.util.Timer;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

@SuppressWarnings("UnusedDeclaration")
public class PromiseEnvironment {
    private static final Timer scheduler = new Timer(true);

    private final CallbackExecutor callbackExecutor;
    private final Executor runnableExecutor;

    public PromiseEnvironment(Executor runnableExecutor, CallbackExecutor callbackExecutor) {
        if (runnableExecutor == null) {
            runnableExecutor = Executors.newCachedThreadPool();
        }

        if (callbackExecutor == null) {
            callbackExecutor = RunnableCallbackExecutor.getInstance();
            callbackExecutor.setRunnableExecutor(runnableExecutor);
        }

        this.runnableExecutor = runnableExecutor;
        this.callbackExecutor = callbackExecutor;
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
            // can use object pool in this point
            return reasonType.newInstance();
        } catch (Exception e) {
            throw new IllegalStateException("Can't create new instance for Event object", e);
        }
    }

    public <T> Promise<T> getPromise() {
        // can use object pool in this point
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